package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.type.InventoryPowerType;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.InventoryUtil.InventoryType;
import io.github.apace100.apoli.util.InventoryUtil.ProcessMode;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import static io.github.apace100.apoli.util.InventoryUtil.modifyInventory;

public class ModifyInventoryAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        InventoryType inventoryType = data.get("inventory_type");
        ProcessMode processMode = data.get("process_mode");
        int limit = data.getInt("limit");

        switch (inventoryType) {
            case INVENTORY:
                modifyInventory(data, entity, null, processMode.getProcessor(), limit);
                break;
            case POWER:
                if (!data.isPresent("power") || !(entity instanceof LivingEntity livingEntity)) return;

                Power targetPower = data.get("power");
                PowerType targetPowerType = PowerHolderComponent.KEY.get(livingEntity).getPowerType(targetPower);

                if (!(targetPowerType instanceof InventoryPowerType inventoryPower)) return;
                modifyInventory(data, livingEntity, inventoryPower, processMode.getProcessor(), limit);
                break;
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("modify_inventory"),
            new SerializableData()
                .add("inventory_type", ApoliDataTypes.INVENTORY_TYPE, InventoryType.INVENTORY)
                .add("process_mode", ApoliDataTypes.PROCESS_MODE, ProcessMode.STACKS)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("slots", ApoliDataTypes.ITEM_SLOTS, null)
                .add("slot", ApoliDataTypes.ITEM_SLOT, null)
                .add("power", ApoliDataTypes.POWER_REFERENCE, null)
                .add("limit", SerializableDataTypes.INT, 0),
            ModifyInventoryAction::action
        );
    }

}
