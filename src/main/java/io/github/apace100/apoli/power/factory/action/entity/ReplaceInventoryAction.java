package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.InventoryPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import static io.github.apace100.apoli.util.InventoryUtil.*;

public class ReplaceInventoryAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        InventoryType inventoryType = data.get("inventory_type");

        switch (inventoryType) {
            case INVENTORY:
                replaceInventory(data, entity, null);
                break;
            case POWER:
                if (!data.isPresent("power") || !(entity instanceof LivingEntity livingEntity)) return;

                PowerType<?> targetPowerType = data.get("power");
                Power targetPower = PowerHolderComponent.KEY.get(livingEntity).getPower(targetPowerType);

                if (!(targetPower instanceof InventoryPower inventoryPower)) return;
                replaceInventory(data, livingEntity, inventoryPower);
                break;
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("replace_inventory"),
            new SerializableData()
                .add("inventory_type", SerializableDataType.enumValue(InventoryType.class), InventoryType.INVENTORY)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("slots", ApoliDataTypes.ITEM_SLOTS, null)
                .add("slot", ApoliDataTypes.ITEM_SLOT, null)
                .add("power", ApoliDataTypes.POWER_TYPE, null)
                .add("stack", SerializableDataTypes.ITEM_STACK),
            ReplaceInventoryAction::action
        );
    }
}
