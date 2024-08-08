package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EffectImmunityPower extends Power {

    protected final Set<RegistryEntry<StatusEffect>> effects = new HashSet<>();
    private final boolean inverted;

    public EffectImmunityPower(PowerType type, LivingEntity entity, boolean inverted) {
        super(type, entity);
        this.inverted = inverted;
    }

    public EffectImmunityPower addEffect(RegistryEntry<StatusEffect> effect) {
        effects.add(effect);
        return this;
    }

    public boolean doesApply(StatusEffectInstance instance) {
        return doesApply(instance.getEffectType());
    }

    public boolean doesApply(RegistryEntry<StatusEffect> effect) {
        return inverted ^ effects.contains(effect);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("effect_immunity"),
            new SerializableData()
                .add("effect", SerializableDataTypes.STATUS_EFFECT_ENTRY, null)
                .add("effects", SerializableDataTypes.STATUS_EFFECT_ENTRIES, null)
                .add("inverted", SerializableDataTypes.BOOLEAN, false),
            data ->
                (type, player) -> {

                    EffectImmunityPower power = new EffectImmunityPower(type, player, data.get("inverted"));

                    data.ifPresent("effect", power::addEffect);
                    data.<List<RegistryEntry<StatusEffect>>>ifPresent("effects", effects -> effects.forEach(power::addEffect));

                    return power;

                })
            .allowCondition();
    }
}
