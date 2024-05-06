package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyMobBehaviorPower;
import io.github.apace100.apoli.power.factory.behavior.types.LookMobBehavior;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;

@Mixin(MoveControl.class)
public abstract class MoveControlMixin {
    @Shadow protected abstract float wrapDegrees(float from, float to, float max);

    @Shadow @Final protected MobEntity entity;

    @Shadow protected double speed;

    @Shadow public abstract void tick();

    @Shadow protected double targetX;

    @Shadow protected double targetZ;

    @Shadow protected float forwardMovement;

    @Shadow protected float sidewaysMovement;

    @Shadow protected double targetY;

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;setYaw(F)V"))
    private boolean apoli$canelSetYaw(MobEntity instance, float n) {
        if (PowerHolderComponent.getPowers(instance, ModifyMobBehaviorPower.class).stream().noneMatch(power -> power.getMobBehavior() instanceof LookMobBehavior lookMobBehavior && lookMobBehavior.shouldMoveBody() && lookMobBehavior.getLookTarget() != null)) {
            instance.setYaw(this.wrapDegrees(instance.getYaw(), n, 90.0F));
        }
        return true;
    }

    @Unique
    private double apoli$previousLookingAngle = Double.NaN;

    @Unique
    private Vec3d apoli$previousTargetPos;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;setMovementSpeed(F)V", ordinal = 1, shift = At.Shift.AFTER))
    private void apoli$handleMovement(CallbackInfo ci) {
        LookMobBehavior behavior = PowerHolderComponent.getPowers(this.entity, ModifyMobBehaviorPower.class).stream().filter(power -> power.getMobBehavior() instanceof LookMobBehavior lookMobBehavior && lookMobBehavior.shouldMoveBody() && lookMobBehavior.getLookTarget() != null).min(Comparator.comparingInt(value -> value.getMobBehavior().getPriority())).map(power -> ((LookMobBehavior)power.getMobBehavior())).orElse(null);
        if (behavior != null) {
            Vec3d targetPos = new Vec3d(targetX, targetY, targetZ);
            if (this.apoli$previousTargetPos == null)
                this.apoli$previousTargetPos = targetPos;
            double targetAngle = Math.atan2(targetZ, targetX);
            double lookingAngle = targetAngle - this.entity.getYaw();
            if (Double.isNaN(apoli$previousLookingAngle) || (apoli$previousLookingAngle - lookingAngle) > 2.0 || apoli$previousTargetPos.squaredDistanceTo(targetPos) > 4.0) {
                this.forwardMovement = (float) (Math.sin(lookingAngle) * this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
                this.sidewaysMovement = (float) (Math.cos(lookingAngle) * this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
                this.apoli$previousLookingAngle = lookingAngle;
                this.apoli$previousTargetPos = targetPos;
            }
             this.entity.setMovementSpeed((float) (this.speed * this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)));
            this.entity.setForwardSpeed(forwardMovement);
            this.entity.setSidewaysSpeed(sidewaysMovement);
        } else {
            apoli$previousLookingAngle = Double.NaN;
            this.apoli$previousTargetPos = null;
        }
    }
}
