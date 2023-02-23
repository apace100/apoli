package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyMobBehaviorPower;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "net.minecraft.entity.mob/EndermanEntity$TeleportTowardsPlayerGoal")
public class EndermanEntityTeleportTowardsPlayerGoalMixin {
    @Shadow private @Nullable PlayerEntity targetPlayer;

    @Shadow @Final private EndermanEntity enderman;

    @Inject(method = "canStart", at = @At("RETURN"))
    private void cancelStart(CallbackInfoReturnable<Boolean> cir) {
        List<ModifyMobBehaviorPower> modifyMobBehaviorPowers = PowerHolderComponent.getPowers(this.enderman, ModifyMobBehaviorPower.class);
        boolean shouldMakePassive = modifyMobBehaviorPowers.stream().anyMatch(power -> power.getMobBehavior().isPassive(this.targetPlayer));

        if (shouldMakePassive) {
            this.targetPlayer = null;
        }
    }
}
