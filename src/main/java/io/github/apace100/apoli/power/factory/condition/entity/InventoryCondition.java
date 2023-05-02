package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.InventoryPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class InventoryCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        InventoryUtil.InventoryType inventoryType = data.get("inventory_type");
        AtomicBoolean hasMatches = new AtomicBoolean();

        switch (inventoryType) {
            case INVENTORY -> hasMatches.set(InventoryUtil.checkInventory(data, entity, null));
            case POWER -> {

                PowerType<?> targetPowerType = data.get("power");
                if (targetPowerType == null) return false;

                Optional<PowerHolderComponent> opt$component = PowerHolderComponent.KEY.maybeGet(entity);
                if (opt$component.isEmpty()) return false;

                Power targetPower = opt$component.get().getPower(targetPowerType);
                if (!(targetPower instanceof InventoryPower inventoryPower)) return false;

                hasMatches.set(InventoryUtil.checkInventory(data, entity, inventoryPower));

            }
        }

        return hasMatches.get();

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("inventory"),
            new SerializableData()
                .add("inventory_type", SerializableDataType.enumValue(InventoryUtil.InventoryType.class), InventoryUtil.InventoryType.INVENTORY)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("slots", ApoliDataTypes.ITEM_SLOTS, null)
                .add("slot", ApoliDataTypes.ITEM_SLOT, null)
                .add("power", ApoliDataTypes.POWER_TYPE, null)
                .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
                .add("compare_to", SerializableDataTypes.INT, 0),
            InventoryCondition::condition
        );
    }

}
