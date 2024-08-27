package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModifyStatusEffectAmplifierPowerType extends ValueModifyingPowerType {

    private final Set<RegistryEntry<StatusEffect>> statusEffects;

    public ModifyStatusEffectAmplifierPowerType(Power power, LivingEntity entity, RegistryEntry<StatusEffect> statusEffect, List<RegistryEntry<StatusEffect>> statusEffects, Modifier modifier, List<Modifier> modifiers) {
        super(power, entity);
        this.statusEffects = new HashSet<>();

        if (statusEffect != null) {
            this.statusEffects.add(statusEffect);
        }

        if (statusEffects != null) {
            this.statusEffects.addAll(statusEffects);
        }

        if (modifier != null) {
            this.addModifier(modifier);
        }

        if (modifiers != null) {
            modifiers.forEach(this::addModifier);
        }

    }

    public boolean doesApply(RegistryEntry<StatusEffect> statusEffect) {
        return statusEffects.isEmpty()
            || statusEffects.contains(statusEffect);
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_status_effect_amplifier"),
            new SerializableData()
                .add("status_effect", SerializableDataTypes.STATUS_EFFECT_ENTRY, null)
                .add("status_effects", SerializableDataTypes.STATUS_EFFECT_ENTRIES, null)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data -> (power, entity) -> new ModifyStatusEffectAmplifierPowerType(power, entity,
                data.get("status_effect"),
                data.get("status_effects"),
                data.get("modifier"),
                data.get("modifiers")
            )
        ).allowCondition();
    }

}
