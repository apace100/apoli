package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.CooldownPowerType;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class TriggerCooldownActionType {

    public static void action(Entity entity, PowerReference power) {

        if (power.getType(entity) instanceof CooldownPowerType cooldown) {
            cooldown.use();
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("trigger_cooldown"),
            new SerializableData()
                .add("power", ApoliDataTypes.POWER_REFERENCE),
            (data, entity) -> action(entity,
                data.get("power")
            )
        );
    }

}
