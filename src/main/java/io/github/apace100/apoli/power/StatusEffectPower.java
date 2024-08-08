package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.LinkedList;
import java.util.List;

public class StatusEffectPower extends Power {

    protected final List<StatusEffectInstance> effects = new LinkedList<>();

    public StatusEffectPower(PowerType type, LivingEntity entity) {
        super(type, entity);
    }
    public StatusEffectPower(PowerType type, LivingEntity entity, StatusEffectInstance effectInstance) {
        super(type, entity);
        addEffect(effectInstance);
    }

    public StatusEffectPower addEffect(RegistryEntry<StatusEffect> effect) {
        return addEffect(effect, 80);
    }

    public StatusEffectPower addEffect(RegistryEntry<StatusEffect> effect, int lingerDuration) {
        return addEffect(effect, lingerDuration, 0);
    }

    public StatusEffectPower addEffect(RegistryEntry<StatusEffect> effect, int lingerDuration, int amplifier) {
        return addEffect(new StatusEffectInstance(effect, lingerDuration, amplifier));
    }

    public StatusEffectPower addEffect(StatusEffectInstance instance) {
        effects.add(instance);
        return this;
    }

    public void applyEffects() {
        effects.stream().map(StatusEffectInstance::new).forEach(entity::addStatusEffect);
    }
}
