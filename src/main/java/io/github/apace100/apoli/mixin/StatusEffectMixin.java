package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.EffectImmunityPowerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StatusEffect.class)
public abstract class StatusEffectMixin {

    @WrapMethod(method = "applyInstantEffect")
    private void apoli$instantEffectImmunity(Entity source, Entity attacker, LivingEntity target, int amplifier, double proximity, Operation<Void> original) {

        if (!PowerHolderComponent.hasPowerType(target, EffectImmunityPowerType.class, p -> p.doesApply(Registries.STATUS_EFFECT.getEntry((StatusEffect) (Object) this)))) {
            original.call(source, attacker, target, amplifier, proximity);
        }

    }

}
