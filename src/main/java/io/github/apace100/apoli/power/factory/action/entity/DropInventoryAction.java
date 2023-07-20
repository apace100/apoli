package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.InventoryPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.InventoryUtil.InventoryType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

import static io.github.apace100.apoli.util.InventoryUtil.dropInventory;

public class DropInventoryAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        InventoryType inventoryType = data.get("inventory_type");

        switch (inventoryType) {
            case INVENTORY -> dropInventory(data, entity, null);
            case POWER -> {

                if (!data.isPresent("power")) return;
                PowerHolderComponent.KEY.maybeGet(entity).ifPresent(
                    powerHolderComponent -> {

                        PowerType<?> targetPowerType = data.get("power");
                        Power targetPower = powerHolderComponent.getPower(targetPowerType);
                        if (!(targetPower instanceof InventoryPower inventoryPower)) return;

                        dropInventory(data, entity, inventoryPower);

                    }
                );

            }
        }

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("drop_inventory"),
            new SerializableData()
                .add("inventory_type", ApoliDataTypes.INVENTORY_TYPE, InventoryType.INVENTORY)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("slots", ApoliDataTypes.ITEM_SLOTS, null)
                .add("slot", ApoliDataTypes.ITEM_SLOT, null)
                .add("power", ApoliDataTypes.POWER_TYPE, null)
                .add("throw_randomly", SerializableDataTypes.BOOLEAN, false)
                .add("retain_ownership", SerializableDataTypes.BOOLEAN, true)
                .add("amount", SerializableDataTypes.INT, 0),
            DropInventoryAction::action
        );
    }

}
