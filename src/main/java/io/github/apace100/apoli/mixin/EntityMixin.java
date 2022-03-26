package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.MovingEntity;
import io.github.apace100.apoli.access.SubmergableEntity;
import io.github.apace100.apoli.access.WaterMovingEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.*;
import io.github.apace100.calio.Calio;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.Fluid;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin implements MovingEntity, SubmergableEntity {

    @Inject(method = "isFireImmune", at = @At("HEAD"), cancellable = true)
    private void makeFullyFireImmune(CallbackInfoReturnable<Boolean> cir) {
        if(PowerHolderComponent.hasPower((Entity)(Object)this, FireImmunityPower.class)) {
            cir.setReturnValue(true);
        }
    }

    @Shadow
    public World world;

    @Shadow
    public abstract double getFluidHeight(Tag<Fluid> fluid);

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    public float distanceTraveled;

    @Shadow
    protected boolean onGround;

    @Shadow @Nullable protected Tag<Fluid> submergedFluidTag;

    @Shadow protected Object2DoubleMap<Tag<Fluid>> fluidHeight;

    @Shadow public abstract boolean collidesWithStateAtPos(BlockPos pos, BlockState state);

    @Shadow public abstract boolean removeScoreboardTag(String tag);

    @Shadow protected abstract void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition);

    @Inject(method = "isTouchingWater", at = @At("HEAD"), cancellable = true)
    private void makeEntitiesIgnoreWater(CallbackInfoReturnable<Boolean> cir) {
        if(PowerHolderComponent.hasPower((Entity)(Object)this, IgnoreWaterPower.class)) {
            if(this instanceof WaterMovingEntity) {
                if(((WaterMovingEntity)this).isInMovementPhase()) {
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

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;<init>(DDD)V"), method = "pushOutOfBlocks", cancellable = true)
    protected void pushOutOfBlocks(double x, double y, double z, CallbackInfo info) {
        List<PhasingPower> powers = PowerHolderComponent.getPowers((Entity)(Object)this, PhasingPower.class);
        if(powers.size() > 0) {
            if(powers.stream().anyMatch(phasingPower -> phasingPower.doesApply(new BlockPos(x, y, z)))) {
                info.cancel();
            }
        }
    }

    @Inject(method = "emitGameEvent(Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;)V", at = @At("HEAD"), cancellable = true)
    private void preventGameEvents(GameEvent event, @Nullable Entity entity, BlockPos pos, CallbackInfo ci) {
        if(entity instanceof LivingEntity) {
            List<PreventGameEventPower> preventingPowers = PowerHolderComponent.getPowers(entity, PreventGameEventPower.class).stream().filter(p -> p.doesPrevent(event)).toList();
            if(preventingPowers.size() > 0) {
                preventingPowers.forEach(p -> p.executeAction(entity));
                ci.cancel();
            }
        }
    }

    @Redirect(method = "method_30022", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;"))
    private VoxelShape preventPhasingSuffocation(BlockState state, BlockView world, BlockPos pos) {
        return state.getCollisionShape(world, pos, ShapeContext.of((Entity)(Object)this));
    }

    private boolean isMoving;
    private float distanceBefore;

    @Inject(method = "move", at = @At("HEAD"))
    private void saveDistanceTraveled(MovementType type, Vec3d movement, CallbackInfo ci) {
        this.isMoving = false;
        this.distanceBefore = this.distanceTraveled;
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V"))
    private void checkIsMoving(MovementType type, Vec3d movement, CallbackInfo ci) {
        if (this.distanceTraveled > this.distanceBefore) {
            this.isMoving = true;
        }
    }

    @Override
    public boolean isSubmergedInLoosely(Tag<Fluid> tag) {
        if(tag == null || submergedFluidTag == null) {
            return false;
        }
        if(tag == submergedFluidTag) {
            return true;
        }
        return Calio.areTagsEqual(Registry.FLUID_KEY, tag, submergedFluidTag);
    }

    @Override
    public double getFluidHeightLoosely(Tag<Fluid> tag) {
        if(tag == null) {
            return 0;
        }
        if(fluidHeight.containsKey(tag)) {
            return fluidHeight.getDouble(tag);
        }
        for(Tag<Fluid> ft : fluidHeight.keySet()) {
            if(Calio.areTagsEqual(Registry.FLUID_KEY, ft, tag)) {
                return fluidHeight.getDouble(ft);
            }
        }
        return 0;
    }

    @Override
    public boolean isMoving() {
        return isMoving;
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "getTeamColorValue", at = @At("RETURN"), cancellable = true)
    private void modifyGlowingColorFromPower(CallbackInfoReturnable<Integer> cir) {
        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
        Entity renderEntity = (Entity) (Object) this;
        AbstractTeam abstractTeam = renderEntity.getScoreboardTeam();
        boolean isUsingTeam = abstractTeam != null && abstractTeam.getColor().getColorValue() != null;
        boolean modified = false;
        List<Float> unmixedRed = new ArrayList<>();
        List<Float> unmixedGreen = new ArrayList<>();
        List<Float> unmixedBlue = new ArrayList<>();

        for (EntityGlowPower power : PowerHolderComponent.getPowers(cameraEntity, EntityGlowPower.class)) {
            if (power.doesApply(renderEntity)) {
                if (!isUsingTeam || !power.usesTeams()) {
                    modified = true;
                    unmixedRed.add(power.getRed());
                    unmixedGreen.add(power.getGreen());
                    unmixedBlue.add(power.getBlue());
                }
            }
        }

        for (SelfGlowPower power : PowerHolderComponent.getPowers(renderEntity, SelfGlowPower.class)) {
            if (!isUsingTeam || !power.usesTeams()) {
                modified = true;
                unmixedRed.add(power.getRed());
                unmixedGreen.add(power.getGreen());
                unmixedBlue.add(power.getBlue());
            }
        }

        if(modified) {
            cir.setReturnValue(MathHelper.packRgb(mixColors(unmixedRed), mixColors(unmixedGreen), mixColors(unmixedBlue)));
        }

    }

    @Unique
    private static float mixColors(List<Float> colors) {
        if(colors.size() == 0) return 1.0F;
        if(colors.size() == 1) return colors.get(0);

        float sum = 0.0F;
        for(float color : colors) {
            sum += color;
        }
        return sum / colors.size();
    }

}
