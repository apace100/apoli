package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class SetOnFireActionType {

    public static void action(Entity entity, float seconds) {
        entity.setOnFireFor(seconds);
    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("set_on_fire"),
            new SerializableData()
                .add("duration", SerializableDataTypes.FLOAT),
            (data, entity) -> action(entity,
                data.get("duration")
            )
        );
    }

}
