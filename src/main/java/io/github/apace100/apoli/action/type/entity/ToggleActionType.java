package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.TogglePowerType;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class ToggleActionType {

    public static void action(Entity entity, PowerReference power) {

        if (power.getType(entity) instanceof TogglePowerType toggle) {
            toggle.onUse();
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("toggle"),
            new SerializableData()
                .add("power", ApoliDataTypes.POWER_REFERENCE),
            (data, entity) -> action(entity,
                data.get("power")
            )
        );
    }

}
