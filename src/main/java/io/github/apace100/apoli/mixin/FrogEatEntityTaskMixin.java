package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.power.factory.behavior.types.HostileMobBehavior;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyMobBehaviorPower;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.FrogEatEntityTask;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FrogEatEntityTask.class)
public class FrogEatEntityTaskMixin {
    @Inject(method = "shouldRun(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/FrogEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/FrogEntity;getPose()Lnet/minecraft/entity/EntityPose;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void allowPowerTargetsAsFrogFood(ServerWorld serverWorld, FrogEntity frogEntity, CallbackInfoReturnable<Boolean> cir, LivingEntity livingEntity, boolean bl) {
        if (PowerHolderComponent.getPowers(frogEntity, ModifyMobBehaviorPower.class).stream().anyMatch(power -> power.getMobBehavior() instanceof HostileMobBehavior && power.getMobBehavior().doesApply(livingEntity))) {
            cir.setReturnValue(bl && frogEntity.getPose() != EntityPose.CROAKING);
        }
    }
}
