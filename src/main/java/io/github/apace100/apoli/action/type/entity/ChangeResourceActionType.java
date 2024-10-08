package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.PowerUtil;
import io.github.apace100.apoli.util.ResourceOperation;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class ChangeResourceActionType {

    public static void action(Entity entity, PowerReference power, ResourceOperation operation, int change) {

        PowerType powerType = power.getType(entity);
        boolean modified = switch (operation) {
            case ADD ->
                PowerUtil.changeResourceValue(powerType, change);
            case SET ->
                PowerUtil.setResourceValue(powerType, change);
        };

        if (modified) {
            PowerHolderComponent.syncPower(entity, power);
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("change_resource"),
            new SerializableData()
                .add("resource", ApoliDataTypes.RESOURCE_REFERENCE)
                .add("operation", ApoliDataTypes.RESOURCE_OPERATION, ResourceOperation.ADD)
                .add("change", SerializableDataTypes.INT),
            (data, entity) -> action(entity,
                data.get("resource"),
                data.get("operation"),
                data.get("change")
            )
        );
    }

}
