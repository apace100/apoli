package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyMobBehaviorPower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(WardenEntity.class)
public class WardenEntityMixin {
    @Inject(method = "isValidTarget", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;world:Lnet/minecraft/world/World;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void modifyTarget(Entity entity, CallbackInfoReturnable<Boolean> cir, LivingEntity livingEntity) {
        List<ModifyMobBehaviorPower> modifyMobBehaviorPowers = PowerHolderComponent.getPowers((MobEntity)(Object)this, ModifyMobBehaviorPower.class);
        boolean shouldMakePassive = modifyMobBehaviorPowers.stream().anyMatch(power -> power.getMobBehavior().isPassive(livingEntity));

        if (shouldMakePassive) {
            cir.setReturnValue(false);
        }
    }
}
