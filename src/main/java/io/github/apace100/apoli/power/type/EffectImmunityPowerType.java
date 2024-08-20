package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EffectImmunityPowerType extends PowerType {

    protected final Set<RegistryEntry<StatusEffect>> effects = new HashSet<>();
    private final boolean inverted;

    public EffectImmunityPowerType(Power power, LivingEntity entity, boolean inverted) {
        super(power, entity);
        this.inverted = inverted;
    }

    public EffectImmunityPowerType addEffect(RegistryEntry<StatusEffect> effect) {
        effects.add(effect);
        return this;
    }

    public boolean doesApply(StatusEffectInstance instance) {
        return doesApply(instance.getEffectType());
    }

    public boolean doesApply(RegistryEntry<StatusEffect> effect) {
        return inverted ^ effects.contains(effect);
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(Apoli.identifier("effect_immunity"),
            new SerializableData()
                .add("effect", SerializableDataTypes.STATUS_EFFECT_ENTRY, null)
                .add("effects", SerializableDataTypes.STATUS_EFFECT_ENTRIES, null)
                .add("inverted", SerializableDataTypes.BOOLEAN, false),
            data -> (power, entity) -> {

                EffectImmunityPowerType powerType = new EffectImmunityPowerType(power, entity, data.get("inverted"));

                data.ifPresent("effect", powerType::addEffect);
                data.<List<RegistryEntry<StatusEffect>>>ifPresent("effects", effects -> effects.forEach(powerType::addEffect));

                return powerType;

            }
        ).allowCondition();
    }
}
