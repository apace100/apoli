package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.*;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataHandlers;
import io.github.apace100.apoli.power.type.*;
import io.github.apace100.apoli.util.ArmPoseReference;
import io.github.apace100.calio.Calio;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.listener.EntityGameEventHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

@Mixin(Entity.class)
public abstract class EntityMixin implements MovingEntity, SubmergableEntity, ModifiedPoseHolder, LeashableEntity {

    @Shadow
    private World world;

    @Shadow
    public abstract double getFluidHeight(TagKey<Fluid> fluid);

    @Shadow
    private boolean onGround;

    @Final
    @Shadow
    @Nullable
    private Set<TagKey<Fluid>> submergedFluidTag;

    @Shadow protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    @Shadow public abstract Vec3d getPos();

    @Shadow public abstract double getX();

    @Shadow public abstract double getY();

    @Shadow public abstract double getZ();

    @Shadow public abstract World getWorld();

    @Shadow @Final protected DataTracker dataTracker;

    @Shadow @Final private Set<String> commandTags;

    @Shadow public abstract Text getName();

    @Shadow public abstract DataTracker getDataTracker();

    @Shadow public abstract void setPose(EntityPose pose);

    @Shadow public abstract EntityPose getPose();

    @Shadow public abstract boolean isSwimming();

    @ModifyReturnValue(method = "isFireImmune", at = @At("RETURN"))
    private boolean apoli$makeFullyFireImmune(boolean original) {
        return original
            || PowerHolderComponent.hasPowerType((Entity) (Object) this, FireImmunityPowerType.class);
    }

    @ModifyReturnValue(method = "isTouchingWater", at = @At("RETURN"))
    private boolean apoli$makeEntitiesIgnoreWater(boolean original) {

        if (!(this instanceof WaterMovingEntity waterMovingEntity)) {
            return original;
        }

        return original
            && !(waterMovingEntity.apoli$isInMovementPhase() && PowerHolderComponent.hasPowerType((Entity) (Object) this, IgnoreWaterPowerType.class));

    }

    @Inject(method = "fall", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onLandedUpon(Lnet/minecraft/world/World;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;F)V"))
    private void invokeActionOnLand(CallbackInfo ci) {
        PowerHolderComponent.withPowerTypes((Entity) (Object) this, ActionOnLandPowerType.class, p -> true, ActionOnLandPowerType::executeAction);
    }

    @ModifyReturnValue(method = "isInvulnerableTo", at = @At("RETURN"))
    private boolean apoli$makeEntitiesInvulnerable(boolean original, DamageSource source) {
        return original
            || PowerHolderComponent.hasPowerType((Entity) (Object) this, InvulnerablePowerType.class, p -> p.doesApply(source));
    }

    @ModifyExpressionValue(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isWet()Z"))
    private boolean apoli$preventExtinguishingFromPowerSwimming(boolean original) {
        return original
            && !(this.isSwimming() && PowerHolderComponent.hasPowerType((Entity) (Object) this, SwimmingPowerType.class));
    }

    @ModifyReturnValue(method = "isInvisible", at = @At("RETURN"))
    private boolean apoli$invisibility(boolean original) {
        return original
            || PowerHolderComponent.hasPowerType((Entity) (Object) this, InvisibilityPowerType.class);
    }

    @WrapOperation(method = "isInvisibleTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isInvisible()Z"))
    private boolean apoli$specificallyInvisibleTo(Entity entity, Operation<Boolean> original, PlayerEntity viewer) {

        List<InvisibilityPowerType> invisibilityPowers = PowerHolderComponent.getPowerTypes(entity, InvisibilityPowerType.class, true);
        if (viewer == null || invisibilityPowers.isEmpty()) {
            return original.call(entity);
        }

        return invisibilityPowers
            .stream()
            .anyMatch(p -> p.isActive() && p.doesApply(viewer));

    }

    //  TODO: Use MixinExtras' @WrapMethod from its new beta releases -eggohito
    @Inject(method = "pushOutOfBlocks", at = @At(value = "NEW", target = "()Lnet/minecraft/util/math/BlockPos$Mutable;"), cancellable = true)
    protected void apoli$ignorePhasingEntities(double x, double y, double z, CallbackInfo ci, @Local BlockPos pos) {

        if (PowerHolderComponent.hasPowerType((Entity) (Object) this, PhasingPowerType.class, p -> p.doesApply(pos))) {
            ci.cancel();
        }

    }

    @Redirect(method = "method_30022", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;"))
    private VoxelShape preventPhasingSuffocation(BlockState state, BlockView world, BlockPos pos) {
        return state.getCollisionShape(world, pos, ShapeContext.of((Entity)(Object)this));
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3d modifyMovementVelocity(Vec3d original, MovementType movementType) {

        if (movementType != MovementType.SELF) {
            return original;
        }

        return new Vec3d(
            PowerHolderComponent.modify((Entity)(Object) this, ModifyVelocityPowerType.class, original.x, p -> p.doesApply(Direction.Axis.X), p -> {}),
            PowerHolderComponent.modify((Entity)(Object) this, ModifyVelocityPowerType.class, original.y, p -> p.doesApply(Direction.Axis.Y), p -> {}),
            PowerHolderComponent.modify((Entity)(Object) this, ModifyVelocityPowerType.class, original.z, p -> p.doesApply(Direction.Axis.Z), p -> {})
        );

    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getLandingPos()Lnet/minecraft/util/math/BlockPos;"))
    private void forceGrounded(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        if(PowerHolderComponent.hasPowerType((Entity)(Object)this, GroundedPowerType.class)) {
            this.onGround = true;
        }
    }

    @Override
    public boolean apoli$isSubmergedInLoosely(TagKey<Fluid> tag) {

        if (tag == null || submergedFluidTag == null) {
            return false;
        }

        return submergedFluidTag.contains(tag);
        //return Calio.areTagsEqual(Registry.FLUID_KEY, tag, submergedFluidTag);
    }

    @Override
    public double apoli$getFluidHeightLoosely(TagKey<Fluid> tag) {
        if(tag == null) {
            return 0;
        }
        if(fluidHeight.containsKey(tag)) {
            return fluidHeight.getDouble(tag);
        }
        for(TagKey<Fluid> ft : fluidHeight.keySet()) {
            if(Calio.areTagsEqual(RegistryKeys.FLUID, ft, tag)) {
                return fluidHeight.getDouble(ft);
            }
        }
        return 0;
    }

    @Environment(EnvType.CLIENT)
    @ModifyReturnValue(method = "getTeamColorValue", at = @At("RETURN"))
    private int apoli$modifyGlowingColorFromPower(int original) {

        //  region Advised by @EdwinMindcraft: a solution for making the hook limited to WorldRenderer ONLY. Uncomment the code in this region when run into unexpected calls to Entity#getTeamColorValue to fix the problem.
//        StackWalker walker = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE), 2);
//        boolean calledByWorldRenderer = walker.walk(stackFrameStream -> stackFrameStream
//            .map(StackWalker.StackFrame::getDeclaringClass)
//            .anyMatch(cls -> cls == WorldRenderer.class));
//
//        if (!calledByWorldRenderer) {
//            return original;
//        }
        //  endregion

        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
        Entity renderedEntity = (Entity) (Object) this;

        AbstractTeam team = renderedEntity.getScoreboardTeam();

        boolean hasTeamColor = team != null && team.getColor().getColorValue() != null;
        int colorAmount = 0;

        float red = 0.0f;
        float green = 0.0f;
        float blue = 0.0f;

        for (EntityGlowPowerType entityGlowPower : PowerHolderComponent.getPowerTypes(cameraEntity, EntityGlowPowerType.class)) {

            if ((hasTeamColor && entityGlowPower.usesTeams()) || !entityGlowPower.doesApply(renderedEntity)) {
                continue;
            }

            red += entityGlowPower.getRed();
            green += entityGlowPower.getGreen();
            blue += entityGlowPower.getBlue();

            colorAmount++;

        }

        for (SelfGlowPowerType selfGlowPower : PowerHolderComponent.getPowerTypes(renderedEntity, SelfGlowPowerType.class)) {

            if ((hasTeamColor && selfGlowPower.usesTeams()) || !selfGlowPower.doesApply(cameraEntity)) {
                continue;
            }

            red += selfGlowPower.getRed();
            green += selfGlowPower.getGreen();
            blue += selfGlowPower.getBlue();

            colorAmount++;

        }

        return colorAmount > 0
            ? MathHelper.packRgb(red / colorAmount, green / colorAmount, blue / colorAmount)
            : original;

    }

    @Inject(method = "updateEventHandler", at = @At("HEAD"))
    private void apoli$updateCustomEventHandlers(BiConsumer<EntityGameEventHandler<?>, ServerWorld> callback, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            PowerHolderComponent.getPowerTypes((Entity) (Object) this, GameEventListenerPowerType.class).forEach(gelp -> callback.accept(gelp.getGameEventHandler(), serverWorld));
        }
    }

    @ModifyExpressionValue(method = "pushAwayFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isConnectedThroughVehicle(Lnet/minecraft/entity/Entity;)Z"))
    private boolean apoli$preventEntityPushing(boolean original, Entity fromEntity) {
        return original || PreventEntityCollisionPowerType.doesApply(fromEntity, (Entity) (Object) this);
    }

    @ModifyReturnValue(method = "collidesWith", at = @At("RETURN"))
    private boolean apoli$preventEntityCollision(boolean original, Entity other) {
        return !PreventEntityCollisionPowerType.doesApply((Entity) (Object) this, other) && original;
    }

    @Unique
    private boolean apoli$movingHorizontally;

    @Unique
    private boolean apoli$movingVertically;

    @Unique
    private double apoli$horizontalMovementValue;

    @Unique
    private double apoli$verticalMovementValue;

    @Unique
    private Vec3d apoli$prevPos;

    @Override
    public boolean apoli$isMovingHorizontally() {
        return apoli$movingHorizontally;
    }

    @Override
    public boolean apoli$isMovingVertically() {
        return apoli$movingVertically;
    }

    @Override
    public double apoli$getHorizontalMovementValue() {
        return apoli$horizontalMovementValue;
    }

    @Override
    public double apoli$getVerticalMovementValue() {
        return apoli$verticalMovementValue;
    }

    @Override
    public boolean apoli$isMoving() {
        return apoli$movingHorizontally || apoli$movingVertically;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void apoli$resetMovingFlags(CallbackInfo ci) {
        this.apoli$movingHorizontally = false;
        this.apoli$movingVertically = false;
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void apoli$setMovingFlags(CallbackInfo ci) {

        if (apoli$prevPos == null) {
            this.apoli$prevPos = this.getPos();
            return;
        }

        double dx = apoli$prevPos.x - this.getX();
        double dy = apoli$prevPos.y - this.getY();
        double dz = apoli$prevPos.z - this.getZ();

        this.apoli$horizontalMovementValue = Math.sqrt(dx * dx + dz * dz);
        this.apoli$verticalMovementValue = Math.sqrt(dy * dy);

        this.apoli$prevPos = this.getPos();

        if (this.apoli$horizontalMovementValue >= 0.01) {
            this.apoli$movingHorizontally = true;
        }

        if (this.apoli$verticalMovementValue >= 0.01) {
            this.apoli$movingVertically = true;
        }

    }

    @Unique
    private static final TrackedData<Set<String>> COMMAND_TAGS = DataTracker.registerData(Entity.class, ApoliDataHandlers.STRING_SET);

    @Unique
    private boolean apoli$hasCommandTagsTracker = true;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;initDataTracker(Lnet/minecraft/entity/data/DataTracker$Builder;)V"))
    private void apoli$registerCommandTagsDataTracker(EntityType<?> type, World world, CallbackInfo ci, @Local DataTracker.Builder builder) {

        try {
            builder.add(COMMAND_TAGS, Set.of());
        }

        catch (Exception e) {
            Apoli.LOGGER.warn("Couldn't register data tracker for command tags for entity {}:", this.getName().getString(), e);
            this.apoli$hasCommandTagsTracker = false;
        }

    }

    @ModifyReturnValue(method = "addCommandTag", at = @At("RETURN"))
    private boolean apoli$trackAddedCommandTag(boolean original) {

        if (original && apoli$hasCommandTagsTracker) {
            this.getDataTracker().set(COMMAND_TAGS, Set.copyOf(this.commandTags));
        }

        return original;

    }

    @ModifyReturnValue(method = "removeCommandTag", at = @At("RETURN"))
    private boolean apoli$trackRemovedCommandTag(boolean original) {

        if (original && apoli$hasCommandTagsTracker) {
            this.getDataTracker().set(COMMAND_TAGS, Set.copyOf(this.commandTags));
        }

        return original;

    }

    @ModifyReturnValue(method = "getCommandTags", at = @At("RETURN"))
    private Set<String> apoli$queryTrackedCommandTags(Set<String> original) {
        return apoli$hasCommandTagsTracker
            ? this.getDataTracker().get(COMMAND_TAGS)
            : original;
    }

    @Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
    private void apoli$trackCommandTagsFromNbt(NbtCompound nbt, CallbackInfo ci) {

        if (apoli$hasCommandTagsTracker) {
            this.getDataTracker().set(COMMAND_TAGS, Set.copyOf(this.commandTags));
        }

    }

    @Redirect(method = "writeNbt", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;commandTags:Ljava/util/Set;"))
    private Set<String> apoli$overrideCommandTagsFieldAccess(Entity entity) {
        return entity.getCommandTags();
    }

    @Unique
    private EntityPose apoli$previousEntityPose;

    @Unique
    private EntityPose apoli$modifiedEntityPose;

    @Unique
    private ArmPoseReference apoli$modifiedArmPose;

    @Override
    public EntityPose apoli$getModifiedEntityPose() {
        return apoli$modifiedEntityPose;
    }

    @Override
    public void apoli$setModifiedEntityPose(EntityPose entityPose) {
        this.apoli$modifiedEntityPose = entityPose;
    }

    @Override
    public ArmPoseReference apoli$getModifiedArmPose() {
        return apoli$modifiedArmPose;
    }

    @Override
    public void apoli$setModifiedArmPose(ArmPoseReference armPose) {
        this.apoli$modifiedArmPose = armPose;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void apoli$overridePose(CallbackInfo ci) {
        PowerHolderComponent.getPowerTypes((Entity) (Object) this, PosePowerType.class)
            .stream()
            .max(Comparator.comparing(PosePowerType::getPriority))
            .ifPresentOrElse(
                posePower -> {

                    if (!((Entity) (Object) this instanceof PlayerEntity) && apoli$previousEntityPose == null) {
                        this.apoli$previousEntityPose = this.getPose();
                    }

                    @Nullable
                    EntityPose entityPose = posePower.getEntityPose();

                    this.apoli$setModifiedEntityPose(entityPose);
                    this.apoli$setModifiedArmPose(posePower.getArmPose());

                    if (entityPose != null) {
                        this.setPose(entityPose);
                    }

                },
                () -> {

                    this.apoli$setModifiedEntityPose(null);
                    this.apoli$setModifiedArmPose(null);

                    if (apoli$previousEntityPose != null) {
                        this.setPose(apoli$previousEntityPose);
                    }

                    this.apoli$previousEntityPose = null;

                }
            );
    }

    @Unique
    private boolean apoli$customLeashed;

    @Override
    public boolean apoli$isCustomLeashed() {
        return apoli$customLeashed;
    }

    @Override
    public void apoli$setCustomLeashed(boolean customLeashed) {
        this.apoli$customLeashed = customLeashed;
    }

}
