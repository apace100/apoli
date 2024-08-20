package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.EntitySetPowerType;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

import java.util.Optional;

public class EntitySetSizeConditionType {

    public static boolean condition(Entity entity, PowerReference power, Comparison comparison, int compareTo) {

        int setSize = Optional.ofNullable(power.getType(entity))
            .filter(powerType -> powerType instanceof EntitySetPowerType)
            .map(powerType -> ((EntitySetPowerType) powerType).size())
            .orElse(0);

        return comparison.compare(setSize, compareTo);

    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("entity_set_size"),
            new SerializableData()
                .add("set", ApoliDataTypes.POWER_REFERENCE)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> condition(entity,
                data.get("set"),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
