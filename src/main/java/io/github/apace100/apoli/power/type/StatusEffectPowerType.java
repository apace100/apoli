package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class StatusEffectPowerType extends PowerType {

    protected final List<StatusEffectInstance> effects = new LinkedList<>();

    public StatusEffectPowerType(List<StatusEffectInstance> effectInstances, Optional<EntityCondition> condition) {
        super(condition);
        this.effects.addAll(effectInstances);
    }

    public StatusEffectPowerType(Optional<EntityCondition> condition) {
        super(condition);
    }

    public StatusEffectPowerType() {

    }

    public StatusEffectPowerType(StatusEffectInstance effectInstance, Optional<EntityCondition> condition) {
        this(condition);
        this.addEffect(effectInstance);
    }

    public StatusEffectPowerType(StatusEffectInstance effectInstance) {
        this(effectInstance, Optional.empty());
    }

    public void addEffect(RegistryEntry<StatusEffect> effect, int duration) {
        addEffect(effect, duration, 0);
    }

    public void addEffect(RegistryEntry<StatusEffect> effect, int duration, int amplifier) {
        addEffect(new StatusEffectInstance(effect, duration, amplifier));
    }

    public void addEffect(StatusEffectInstance instance) {
        effects.add(instance);
    }

    public void applyEffects() {
        effects.stream().map(StatusEffectInstance::new).forEach(getHolder()::addStatusEffect);
    }

}
