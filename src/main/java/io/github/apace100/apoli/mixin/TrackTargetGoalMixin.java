package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyMobBehaviorPower;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
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

import java.util.List;

@Mixin(TrackTargetGoal.class)
public abstract class TrackTargetGoalMixin extends Goal {
    @Shadow @Final protected MobEntity mob;

    @Shadow @Nullable protected LivingEntity target;

    @Inject(method = "shouldContinue", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void modifyTarget(CallbackInfoReturnable<Boolean> cir) {
        List<ModifyMobBehaviorPower> modifyMobBehaviorPowers = PowerHolderComponent.getPowers(this.mob, ModifyMobBehaviorPower.class);
        boolean shouldMakePassive = modifyMobBehaviorPowers.stream().anyMatch(power -> power.getMobBehavior().isPassive(this.target));

        if (shouldMakePassive) {
            this.target = null;
        }
    }
}
