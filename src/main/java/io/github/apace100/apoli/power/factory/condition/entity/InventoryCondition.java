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
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class InventoryCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        Set<InventoryUtil.InventoryType> inventoryTypes = data.get("inventory_types");
        Comparison comparison = data.get("comparison");

        boolean result = false;
        int compareTo = data.get("compare_to");
        int matches = 0;

        if (inventoryTypes.contains(InventoryUtil.InventoryType.INVENTORY)) {
            matches += InventoryUtil.checkInventory(data, entity, null);
            result = comparison.compare(matches, compareTo);
        }
        if (inventoryTypes.contains(InventoryUtil.InventoryType.POWER)) {

            PowerType<?> targetPowerType = data.get("power");
            if (targetPowerType == null) {
                return result;
            }

            Optional<PowerHolderComponent> opt$component = PowerHolderComponent.KEY.maybeGet(entity);
            if (opt$component.isEmpty()) {
                return result;
            }

            Power targetPower = opt$component.get().getPower(targetPowerType);
            if (!(targetPower instanceof InventoryPower inventoryPower)) {
                return result;
            }

            matches += InventoryUtil.checkInventory(data, entity, inventoryPower);
            result = comparison.compare(matches, compareTo);

        }

        return result;

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("inventory"),
            new SerializableData()
                .add("inventory_types", ApoliDataTypes.INVENTORY_TYPE_SET, EnumSet.of(InventoryUtil.InventoryType.INVENTORY))
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
