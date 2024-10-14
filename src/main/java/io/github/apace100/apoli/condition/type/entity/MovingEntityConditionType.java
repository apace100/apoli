package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.access.MovingEntity;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;

public class MovingEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<MovingEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("horizontally", SerializableDataTypes.BOOLEAN, true)
            .add("vertically", SerializableDataTypes.BOOLEAN, true),
        data -> new MovingEntityConditionType(
            data.get("horizontally"),
            data.get("vertically")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("horizontally", conditionType.horizontally)
            .set("vertically", conditionType.vertically)
    );

    private final boolean horizontally;
    private final boolean vertically;

    public MovingEntityConditionType(boolean horizontally, boolean vertically) {
        this.horizontally = horizontally;
        this.vertically = vertically;
    }

    @Override
    public boolean test(Entity entity) {

        if (entity instanceof MovingEntity movingEntity) {
            return (horizontally && movingEntity.apoli$isMovingHorizontally())
                || (vertically && movingEntity.apoli$isMovingVertically());
        }

        else {
            return false;
        }

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.MOVING;
    }

}
