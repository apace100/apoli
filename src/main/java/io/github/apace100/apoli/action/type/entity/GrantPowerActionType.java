package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class GrantPowerActionType {

    public static void action(Entity entity, PowerReference power, Identifier source) {
        PowerHolderComponent.grantPower(entity, power, source, true);
    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("grant_power"),
            new SerializableData()
                .add("power", ApoliDataTypes.POWER_REFERENCE)
                .add("source", SerializableDataTypes.IDENTIFIER),
            (data, entity) -> action(entity,
                data.get("power"),
                data.get("source")
            )
        );
    }

}
