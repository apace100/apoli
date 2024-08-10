package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class FeedActionType {

    public static void action(Entity entity, int nutrition, float saturation) {

        if (entity instanceof PlayerEntity player) {
            player.getHungerManager().add(nutrition, saturation);
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("feed"),
            new SerializableData()
                .add("nutrition", SerializableDataTypes.INT)
                .add("saturation", SerializableDataTypes.FLOAT),
            (data, entity) -> action(entity,
                data.get("nutrition"),
                data.get("saturation")
            )
        );
    }

}
