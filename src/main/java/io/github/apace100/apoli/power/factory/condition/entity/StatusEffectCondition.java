package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

public class StatusEffectCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }

        StatusEffectInstance statusEffectInstance = livingEntity.getStatusEffect(data.get("effect"));
        if (statusEffectInstance == null) {
            return false;
        }

        int duration = statusEffectInstance.getDuration();
        int amplifier = statusEffectInstance.getAmplifier();

        return (duration <= data.getInt("max_duration") && duration >= data.getInt("min_duration"))
            && (amplifier <= data.getInt("max_amplifier") && amplifier >= data.getInt("min_amplifier"));

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("status_effect"),
            new SerializableData()
                .add("effect", SerializableDataTypes.STATUS_EFFECT)
                .add("min_amplifier", SerializableDataTypes.INT, 0)
                .add("max_amplifier", SerializableDataTypes.INT, Integer.MAX_VALUE)
                .add("min_duration", SerializableDataTypes.INT, -1)
                .add("max_duration", SerializableDataTypes.INT, Integer.MAX_VALUE),
            StatusEffectCondition::condition
        );
    }

}
