package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;

public class StatusEffectConditionType {

    public static boolean condition(Entity entity, RegistryEntry<StatusEffect> effect, int minAmplifier, int maxAmplifier, int minDuration, int maxDuration) {

        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        StatusEffectInstance effectInstance = living.getStatusEffect(effect);
        if (effectInstance == null) {
            return false;
        }

        int duration = effectInstance.getDuration();
        int amplifier = effectInstance.getAmplifier();

        return (duration <= maxDuration && duration >= minDuration)
            && (amplifier <= maxAmplifier && amplifier >= minAmplifier);

    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("status_effect"),
            new SerializableData()
                .add("effect", SerializableDataTypes.STATUS_EFFECT_ENTRY)
                .add("min_amplifier", SerializableDataTypes.INT, 0)
                .add("max_amplifier", SerializableDataTypes.INT, Integer.MAX_VALUE)
                .add("min_duration", SerializableDataTypes.INT, -1)
                .add("max_duration", SerializableDataTypes.INT, Integer.MAX_VALUE),
            (data, entity) -> condition(entity,
                data.get("effect"),
                data.get("min_amplifier"),
                data.get("max_amplifier"),
                data.get("min_duration"),
                data.get("max_duration")
            )
        );
    }

}
