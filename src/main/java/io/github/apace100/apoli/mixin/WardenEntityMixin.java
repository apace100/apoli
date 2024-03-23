package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyMobBehaviorPower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(WardenEntity.class)
public class WardenEntityMixin {
    @ModifyReturnValue(method = "isValidTarget", at = @At(value = "RETURN"))
    private boolean modifyTarget(boolean original, Entity entity) {
        List<ModifyMobBehaviorPower> modifyMobBehaviorPowers = PowerHolderComponent.getPowers((MobEntity)(Object)this, ModifyMobBehaviorPower.class);

        if (!(entity instanceof LivingEntity living)) {
            // This should not be the case, but I'm doing this for casting’s sake anyway.
            return original;
        }

        boolean shouldMakePassive = modifyMobBehaviorPowers.stream().anyMatch(power -> power.getMobBehavior().isPassive(living));

        if (shouldMakePassive) {
            return true;
        }
        return  original;
    }
}
