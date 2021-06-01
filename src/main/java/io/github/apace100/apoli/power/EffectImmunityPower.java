package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;

public class EffectImmunityPower extends Power {

    protected final HashSet<StatusEffect> effects = new HashSet<>();

    public EffectImmunityPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }
    public EffectImmunityPower(PowerType<?> type, LivingEntity entity, StatusEffect effect) {
        super(type, entity);
        addEffect(effect);
    }

    public EffectImmunityPower addEffect(StatusEffect effect) {
        effects.add(effect);
        return this;
    }

    public boolean doesApply(StatusEffectInstance instance) {
        return doesApply(instance.getEffectType());
    }

    public boolean doesApply(StatusEffect effect) {
        return effects.contains(effect);
    }
}
