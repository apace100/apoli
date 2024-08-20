package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.power.Power;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

public class SimpleStatusEffectPowerType extends StatusEffectPowerType {

    public SimpleStatusEffectPowerType(Power power, LivingEntity entity) {
        super(power, entity);
    }

    public SimpleStatusEffectPowerType(Power power, LivingEntity entity, StatusEffectInstance effectInstance) {
        super(power, entity, effectInstance);
    }

}
