package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;

public class SetFallDistanceEntityActionType extends EntityActionType {

    public static final DataObjectFactory<SetFallDistanceEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("fall_distance", SerializableDataTypes.FLOAT),
        data -> new SetFallDistanceEntityActionType(
            data.get("fall_distance")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("fall_distance", actionType.fallDistance)
    );

    private final float fallDistance;

    public SetFallDistanceEntityActionType(float fallDistance) {
        this.fallDistance = fallDistance;
    }

    @Override
    protected void execute(Entity entity) {
        entity.fallDistance = fallDistance;
    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.SET_FALL_DISTANCE;
    }

}
