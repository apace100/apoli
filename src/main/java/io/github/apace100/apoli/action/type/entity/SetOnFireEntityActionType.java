package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class SetOnFireEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<SetOnFireEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("duration", SerializableDataTypes.POSITIVE_FLOAT),
        data -> new SetOnFireEntityActionType(
            data.get("duration")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("duration", actionType.duration)
    );

    private final float duration;

    public SetOnFireEntityActionType(float duration) {
        this.duration = duration;
    }

    @Override
    protected void execute(Entity entity) {
        entity.setOnFireFor(duration);
    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.SET_ON_FIRE;
    }

}
