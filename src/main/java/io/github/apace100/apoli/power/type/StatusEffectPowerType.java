package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.power.Power;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.LinkedList;
import java.util.List;

public class StatusEffectPowerType extends PowerType {

    protected final List<StatusEffectInstance> effects = new LinkedList<>();

    public StatusEffectPowerType(Power power, LivingEntity entity) {
        super(power, entity);
    }
    public StatusEffectPowerType(Power power, LivingEntity entity, StatusEffectInstance effectInstance) {
        super(power, entity);
        addEffect(effectInstance);
    }

    public StatusEffectPowerType addEffect(RegistryEntry<StatusEffect> effect) {
        return addEffect(effect, 80);
    }

    public StatusEffectPowerType addEffect(RegistryEntry<StatusEffect> effect, int lingerDuration) {
        return addEffect(effect, lingerDuration, 0);
    }

    public StatusEffectPowerType addEffect(RegistryEntry<StatusEffect> effect, int lingerDuration, int amplifier) {
        return addEffect(new StatusEffectInstance(effect, lingerDuration, amplifier));
    }

    public StatusEffectPowerType addEffect(StatusEffectInstance instance) {
        effects.add(instance);
        return this;
    }

    public void applyEffects() {
        effects.stream().map(StatusEffectInstance::new).forEach(entity::addStatusEffect);
    }
}
