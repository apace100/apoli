package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.MovingEntity;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class MovingCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return (data.getBoolean("horizontally") && ((MovingEntity) entity).apoli$isMovingHorizontally())
            || (data.getBoolean("vertically") && ((MovingEntity) entity).apoli$isMovingVertically());
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("moving"),
            new SerializableData()
                .add("horizontally", SerializableDataTypes.BOOLEAN, true)
                .add("vertically", SerializableDataTypes.BOOLEAN, true),
            MovingCondition::condition
        );
    }

}
