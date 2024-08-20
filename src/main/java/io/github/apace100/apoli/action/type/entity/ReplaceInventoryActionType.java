package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.type.InventoryPowerType;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.InventoryUtil.InventoryType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

import static io.github.apace100.apoli.util.InventoryUtil.replaceInventory;

//  TODO: Refactor this to follow the format of other condition types -eggohito
public class ReplaceInventoryActionType {

    public static void action(SerializableData.Instance data, Entity entity) {

        InventoryType inventoryType = data.get("inventory_type");

        switch (inventoryType) {
            case INVENTORY -> replaceInventory(data, entity, null);
            case POWER -> {

                if (!data.isPresent("power")) return;
                PowerHolderComponent.KEY.maybeGet(entity).ifPresent(
                    powerHolderComponent -> {

                        Power targetPower = data.get("power");
                        PowerType targetPowerType = powerHolderComponent.getPowerType(targetPower);
                        if (!(targetPowerType instanceof InventoryPowerType inventoryPower)) return;

                        replaceInventory(data, entity, inventoryPower);

                    }
                );

            }
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(Apoli.identifier("replace_inventory"),
            new SerializableData()
                .add("inventory_type", ApoliDataTypes.INVENTORY_TYPE, InventoryType.INVENTORY)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("slots", ApoliDataTypes.ITEM_SLOTS, null)
                .add("slot", ApoliDataTypes.ITEM_SLOT, null)
                .add("power", ApoliDataTypes.POWER_REFERENCE, null)
                .add("stack", SerializableDataTypes.ITEM_STACK)
                .add("merge_nbt", SerializableDataTypes.BOOLEAN, false),
            ReplaceInventoryActionType::action
        );
    }

}
