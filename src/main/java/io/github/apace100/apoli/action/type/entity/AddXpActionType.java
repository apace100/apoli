package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AddXpActionType {

    public static void action(Entity entity, int points, int levels) {

        if (!(entity instanceof PlayerEntity player)) {
            return;
        }

        player.addExperience(points);
        player.addExperienceLevels(levels);

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("add_xp"),
            new SerializableData()
                .add("points", SerializableDataTypes.INT, 0)
                .add("levels", SerializableDataTypes.INT, 0),
            (data, entity) -> action(entity,
                data.get("points"),
                data.get("levels")
            )
        );
    }

}
