package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.MovingEntity;
import io.github.apace100.apoli.access.SubmergableEntity;
import io.github.apace100.apoli.access.WaterMovingEntity;
import io.github.apace100.apoli.component.CommandTagComponent;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.*;
import io.github.apace100.calio.Calio;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.world.ServerWorld;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

@Mixin(Entity.class)
public abstract class EntityMixin implements MovingEntity, SubmergableEntity {

    @Inject(method = "isFireImmune", at = @At("HEAD"), cancellable = true)
    private void makeFullyFireImmune(CallbackInfoReturnable<Boolean> cir) {
        if(PowerHolderComponent.hasPower((Entity)(Object)this, FireImmunityPower.class)) {
            cir.setReturnValue(true);
        }
    }

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

    @Shadow public abstract Set<String> getCommandTags();

    @Inject(method = "isTouchingWater", at = @At("HEAD"), cancellable = true)
    private void makeEntitiesIgnoreWater(CallbackInfoReturnable<Boolean> cir) {
        if(PowerHolderComponent.hasPower((Entity)(Object)this, IgnoreWaterPower.class)) {
            if(this instanceof WaterMovingEntity) {
                if(((WaterMovingEntity)this).apoli$isInMovementPhase()) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "fall", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onLandedUpon(Lnet/minecraft/world/World;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;F)V"))
    private void invokeActionOnLand(CallbackInfo ci) {
        List<ActionOnLandPower> powers = PowerHolderComponent.getPowers((Entity)(Object)this, ActionOnLandPower.class);
        powers.forEach(ActionOnLandPower::executeAction);
    }

    @Inject(at = @At("HEAD"), method = "isInvulnerableTo", cancellable = true)
    private void makeOriginInvulnerable(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if((Object)this instanceof LivingEntity) {
            PowerHolderComponent component = PowerHolderComponent.KEY.get(this);
            if(component.getPowers(InvulnerablePower.class).stream().anyMatch(inv -> inv.doesApply(damageSource))) {
                cir.setReturnValue(true);
            }
        }
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isWet()Z"))
    private boolean preventExtinguishingFromSwimming(Entity entity) {
        if(PowerHolderComponent.hasPower(entity, SwimmingPower.class) && entity.isSwimming() && !(getFluidHeight(FluidTags.WATER) > 0)) {
            return false;
        }
        return entity.isWet();
    }

    @Inject(at = @At("HEAD"), method = "isInvisible", cancellable = true)
    private void phantomInvisibility(CallbackInfoReturnable<Boolean> info) {
        if(PowerHolderComponent.hasPower((Entity)(Object)this, InvisibilityPower.class)) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "isInvisibleTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getScoreboardTeam()Lnet/minecraft/scoreboard/AbstractTeam;"), cancellable = true)
    private void invisibilityException(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (PowerHolderComponent.hasPower((Entity) (Object) this, InvisibilityPower.class, p -> !p.doesApply(player))) cir.setReturnValue(false);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;ofFloored(DDD)Lnet/minecraft/util/math/BlockPos;"), method = "pushOutOfBlocks", cancellable = true)
    protected void pushOutOfBlocks(double x, double y, double z, CallbackInfo info) {
        List<PhasingPower> powers = PowerHolderComponent.getPowers((Entity)(Object)this, PhasingPower.class);
        if(!powers.isEmpty()) {
            if(powers.stream().anyMatch(phasingPower -> phasingPower.doesApply(BlockPos.ofFloored(x, y, z)))) {
                info.cancel();
            }
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
            PowerHolderComponent.modify((Entity)(Object) this, ModifyVelocityPower.class, original.x, p -> p.axes.contains(Direction.Axis.X), null),
            PowerHolderComponent.modify((Entity)(Object) this, ModifyVelocityPower.class, original.y, p -> p.axes.contains(Direction.Axis.Y), null),
            PowerHolderComponent.modify((Entity)(Object) this, ModifyVelocityPower.class, original.z, p -> p.axes.contains(Direction.Axis.Z), null)
        );

    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getLandingPos()Lnet/minecraft/util/math/BlockPos;"))
    private void forceGrounded(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        if(PowerHolderComponent.hasPower((Entity)(Object)this, GroundedPower.class)) {
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
    @Inject(method = "getTeamColorValue", at = @At("RETURN"), cancellable = true)
    private void modifyGlowingColorFromPower(CallbackInfoReturnable<Integer> cir) {

/*

        Advised by @EdwinMindcraft: a solution making the hook limited to WorldRenderer ONLY.
        Remove this comment when run into unexpected call to Entity#getTeamColorValue to fix the problem.

        StackWalker walker = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE), 2);
        boolean calledByWorldRenderer = walker.walk(s -> s.map(StackWalker.StackFrame::getDeclaringClass).anyMatch(cls -> cls == WorldRenderer.class));
        if (!calledByWorldRenderer) {
            return;
        }

*/

        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
        Entity renderEntity = (Entity) (Object) this;
        AbstractTeam abstractTeam = renderEntity.getScoreboardTeam();
        boolean isUsingTeam = abstractTeam != null && abstractTeam.getColor().getColorValue() != null;
        int colorAmount = 0;
        float r = 0.0F;
        float g = 0.0F;
        float b = 0.0F;

        for (EntityGlowPower power : PowerHolderComponent.getPowers(cameraEntity, EntityGlowPower.class)) {
            if (power.doesApply(renderEntity) && !(isUsingTeam && power.usesTeams())) {
                colorAmount++;
                r += power.getRed();
                g += power.getGreen();
                b += power.getBlue();
            }
        }

        for (SelfGlowPower power : PowerHolderComponent.getPowers(renderEntity, SelfGlowPower.class)) {
            if (!(isUsingTeam && power.usesTeams())) {
                colorAmount++;
                r += power.getRed();
                g += power.getGreen();
                b += power.getBlue();
            }
        }

        if(colorAmount > 0) {
            cir.setReturnValue(MathHelper.packRgb(r / colorAmount, g / colorAmount, b / colorAmount));
        }

    }

    @Inject(method = "updateEventHandler", at = @At("HEAD"))
    private void apoli$updateCustomEventHandlers(BiConsumer<EntityGameEventHandler<?>, ServerWorld> callback, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            PowerHolderComponent.getPowers((Entity) (Object) this, GameEventListenerPower.class).forEach(gelp -> callback.accept(gelp.getGameEventHandler(), serverWorld));
        }
    }

    @ModifyExpressionValue(method = "pushAwayFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isConnectedThroughVehicle(Lnet/minecraft/entity/Entity;)Z"))
    private boolean apoli$preventEntityPushing(boolean original, Entity fromEntity) {
        return original || PreventEntityCollisionPower.doesApply(fromEntity, (Entity) (Object) this);
    }

    @ModifyReturnValue(method = "collidesWith", at = @At("RETURN"))
    private boolean apoli$preventEntityCollision(boolean original, Entity other) {
        return !PreventEntityCollisionPower.doesApply((Entity) (Object) this, other) && original;
    }

    @Unique
    private boolean apoli$movingHorizontally;

    @Unique
    private boolean apoli$movingVertically;

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

        this.apoli$prevPos = this.getPos();

        if (Math.sqrt(dx * dx + dz * dz) >= 0.01) {
            this.apoli$movingHorizontally = true;
        }

        if (Math.sqrt(dy * dy) >= 0.01) {
            this.apoli$movingVertically = true;
        }

    }

    @Unique
    private boolean apoli$shouldSyncCommandTags = false;

    @Inject(method = "addCommandTag", at = @At("TAIL"))
    private void apoli$addCommandTagToComponent(String tag, CallbackInfoReturnable<Boolean> cir) {

        boolean result = CommandTagComponent.KEY.get(this).addTag(tag);

        if (result) {
            apoli$shouldSyncCommandTags = true;
        }

    }

    @Inject(method = "removeScoreboardTag", at = @At("TAIL"))
    private void apoli$removeCommandTagFromComponent(String tag, CallbackInfoReturnable<Boolean> cir) {

        boolean result = CommandTagComponent.KEY.get(this).removeTag(tag);

        if (result) {
            apoli$shouldSyncCommandTags = true;
        }

    }

    @ModifyReturnValue(method = "getCommandTags", at = @At("RETURN"))
    private Set<String> apoli$overrideCommandTags(Set<String> original) {
        return CommandTagComponent.KEY.get(this).getTags();
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void apoli$syncCommandTags(CallbackInfo ci) {

        if (!apoli$shouldSyncCommandTags || this.getWorld().isClient) {
            return;
        }

        CommandTagComponent.KEY.get(this).setTags(((EntityAccessor) this).getOriginalCommandTags());
        CommandTagComponent.KEY.sync(this);

        apoli$shouldSyncCommandTags = false;

    }

}
