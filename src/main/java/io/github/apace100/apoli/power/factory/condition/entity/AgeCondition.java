package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.apoli.util.Comparison;
import net.minecraft.entity.Entity;

public class AgeCondition {
    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return ((Comparison)data.get("comparison")).compare(entity.age, data.getInt("compare_to"));
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("age"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
                .add("compare_to", SerializableDataTypes.INT, 1),
            AgeCondition::condition
        );
    }
}
