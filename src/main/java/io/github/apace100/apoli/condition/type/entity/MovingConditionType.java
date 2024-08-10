package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.MovingEntity;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class MovingConditionType {

    public static boolean condition(Entity entity, boolean horizontally, boolean vertically) {
        return (horizontally && ((MovingEntity) entity).apoli$isMovingHorizontally())
            || (vertically && ((MovingEntity) entity).apoli$isMovingVertically());
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("moving"),
            new SerializableData()
                .add("horizontally", SerializableDataTypes.BOOLEAN, true)
                .add("vertically", SerializableDataTypes.BOOLEAN, true),
            (data, entity) -> condition(entity,
                data.get("horizontally"),
                data.get("vertically")
            )
        );
    }

}
