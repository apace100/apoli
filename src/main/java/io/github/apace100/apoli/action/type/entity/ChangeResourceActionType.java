package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.CooldownPowerType;
import io.github.apace100.apoli.power.type.VariableIntPowerType;
import io.github.apace100.apoli.util.ResourceOperation;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class ChangeResourceActionType {

    public static void action(Entity entity, PowerReference power, ResourceOperation operation, int change) {

        int oldValue = 0;
        int newValue = 0;

        switch (power.getType(entity)) {
            case VariableIntPowerType varInt -> {

                oldValue = varInt.getValue();
                newValue = processValue(operation, oldValue, change);

                varInt.setValue(newValue);

            }
            case CooldownPowerType cooldown -> {

                oldValue = cooldown.getRemainingTicks();
                newValue = processValue(operation, oldValue, change);

                cooldown.setCooldown(newValue);

            }
            case null, default -> {

            }
        }

        if (oldValue != newValue) {
            PowerHolderComponent.syncPower(entity, power);
        }

    }

    private static int processValue(ResourceOperation operation, int oldValue, int newValue) {
        return switch (operation) {
            case ADD ->
                oldValue + newValue;
            case SET ->
                newValue;
        };
    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("change_resource"),
            new SerializableData()
                .add("resource", ApoliDataTypes.POWER_REFERENCE)
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
