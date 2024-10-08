package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.util.PowerUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

import java.util.List;

public class ModifyResourceActionType {

    public static void action(Entity entity, PowerReference power, Modifier modifier) {

        if (PowerUtil.modifyResourceValue(power.getType(entity), List.of(modifier))) {
            PowerHolderComponent.syncPower(entity, power);
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("modify_resource"),
            new SerializableData()
                .add("resource", ApoliDataTypes.RESOURCE_REFERENCE)
                .add("modifier", Modifier.DATA_TYPE),
            (data, entity) -> action(entity,
                data.get("resource"),
                data.get("modifier")
            )
        );
    }
}
