package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyMobBehaviorPower;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(TrackTargetGoal.class)
public class TrackTargetGoalMixin {

    @Shadow @Final protected MobEntity mob;

    @Shadow @Nullable protected LivingEntity target;

    @Inject(method = "shouldContinue", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/ai/goal/TrackTargetGoal;target:Lnet/minecraft/entity/LivingEntity;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void apoli$preventRevengeGoal(CallbackInfoReturnable<Boolean> cir, LivingEntity livingEntity) {
        if (PowerHolderComponent.getPowers(this.mob, ModifyMobBehaviorPower.class).stream().anyMatch(power -> power.getMobBehavior().isPassive(livingEntity))) {
            this.target = null;
            cir.setReturnValue(false);
        }
    }
}
