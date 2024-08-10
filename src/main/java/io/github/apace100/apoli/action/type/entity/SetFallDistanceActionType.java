package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class SetFallDistanceActionType {

    public static void action(Entity entity, float fallDistance) {
        entity.fallDistance = fallDistance;
    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("set_fall_distance"),
            new SerializableData()
                .add("fall_distance", SerializableDataTypes.FLOAT),
            (data, entity) -> action(entity,
                data.get("fall_distance")
            )
        );
    }

}
