package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;

public class StatusEffectEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<StatusEffectEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("effect", SerializableDataTypes.STATUS_EFFECT_ENTRY)
            .add("min_amplifier", SerializableDataTypes.INT, 0)
            .add("max_amplifier", SerializableDataTypes.INT, Integer.MAX_VALUE)
            .add("min_duration", SerializableDataTypes.INT, -1)
            .add("max_duration", SerializableDataTypes.INT, Integer.MAX_VALUE),
        data -> new StatusEffectEntityConditionType(
            data.get("effect"),
            data.get("min_amplifier"),
            data.get("max_amplifier"),
            data.get("min_duration"),
            data.get("max_duration")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("effect", conditionType.effect)
            .set("min_amplifier", conditionType.minAmplifier)
            .set("max_amplifier", conditionType.maxAmplifier)
            .set("min_duration", conditionType.minDuration)
            .set("max_duration", conditionType.maxDuration)
    );

    private final RegistryEntry<StatusEffect> effect;

    private final int minAmplifier;
    private final int maxAmplifier;

    private final int minDuration;
    private final int maxDuration;

    public StatusEffectEntityConditionType(RegistryEntry<StatusEffect> effect, int minAmplifier, int maxAmplifier, int minDuration, int maxDuration) {
        this.effect = effect;
        this.minAmplifier = minAmplifier;
        this.maxAmplifier = maxAmplifier;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    @Override
    public boolean test(Entity entity) {

        if (entity instanceof LivingEntity livingEntity) {

            StatusEffectInstance effectInstance = livingEntity.getStatusEffect(effect);

            if (effectInstance != null) {

                int duration = effectInstance.getDuration();
                int amplifier = effectInstance.getAmplifier();

                return (duration <= maxDuration && duration >= minDuration)
                    && (amplifier <= maxAmplifier && amplifier >= minAmplifier);

            }

            else {
                return false;
            }

        }

        else {
            return false;
        }

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.STATUS_EFFECT;
    }

}
