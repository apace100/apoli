package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;

public class SimpleStatusEffectPower extends StatusEffectPower {
    public SimpleStatusEffectPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public SimpleStatusEffectPower(PowerType<?> type, LivingEntity entity, StatusEffectInstance effectInstance) {
        super(type, entity, effectInstance);
    }
}
