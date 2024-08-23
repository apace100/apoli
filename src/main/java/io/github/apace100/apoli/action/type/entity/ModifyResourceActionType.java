package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.CooldownPowerType;
import io.github.apace100.apoli.power.type.VariableIntPowerType;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class ModifyResourceActionType {

    public static void action(Entity entity, PowerReference power, Modifier modifier) {

        int oldValue = 0;
        int newValue = 0;

        switch (power.getType(entity)) {
            case VariableIntPowerType varInt -> {

                oldValue = varInt.getValue();
                newValue = (int) modifier.apply(entity, oldValue);

                varInt.setValue(newValue);

            }
            case CooldownPowerType cooldown -> {

                oldValue = cooldown.getRemainingTicks();
                newValue = Math.max((int) modifier.apply(entity, oldValue), 0);

                cooldown.modify(newValue - oldValue);

            }
            case null, default -> {

            }
        }

        if (newValue != oldValue) {
            PowerHolderComponent.syncPower(entity, power);
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(Apoli.identifier("modify_resource"),
            new SerializableData()
                .add("resource", ApoliDataTypes.POWER_REFERENCE)
                .add("modifier", Modifier.DATA_TYPE),
            (data, entity) -> action(entity,
                data.get("resource"),
                data.get("modifier")
            )
        );
    }
}
