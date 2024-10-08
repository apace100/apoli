package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.util.PowerUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class SetResourceActionType {

    public static void action(Entity entity, PowerReference power, int value) {

        if (PowerUtil.setResourceValue(power.getType(entity), value)) {
            PowerHolderComponent.syncPower(entity, power);
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("set_resource"),
            new SerializableData()
                .add("resource", ApoliDataTypes.RESOURCE_REFERENCE)
                .add("value", SerializableDataTypes.INT),
            (data, entity) -> action(entity,
                data.get("resource"),
                data.get("value")
            )
        );
    }

}
