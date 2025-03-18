package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.InventoryPowerType;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.SlotRange;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class InventoryEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<InventoryEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("inventory_types", ApoliDataTypes.INVENTORY_TYPE_SET, EnumSet.allOf(InventoryUtil.InventoryType.class))
            .add("process_mode", ApoliDataTypes.PROCESS_MODE, InventoryUtil.ProcessMode.ITEMS)
            .add("power", ApoliDataTypes.POWER_REFERENCE.optional(), Optional.empty())
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("slot", ApoliDataTypes.SLOT_RANGE, null)
            .addFunctionedDefault("slots", ApoliDataTypes.SLOT_RANGES, data -> MiscUtil.singletonListOrEmpty(data.get("slot")))
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
            .add("compare_to", SerializableDataTypes.INT, 0),
        data -> new InventoryEntityConditionType(
            data.get("inventory_types"),
            data.get("process_mode"),
            data.get("power"),
            data.get("item_condition"),
            data.get("slots"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("inventory_types", conditionType.inventoryTypes)
            .set("process_mode", conditionType.processMode)
            .set("power", conditionType.power)
            .set("item_condition", conditionType.itemCondition)
            .set("slots", conditionType.slotRanges)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final EnumSet<InventoryUtil.InventoryType> inventoryTypes;
    private final InventoryUtil.ProcessMode processMode;

    private final Optional<PowerReference> power;
    private final Optional<ItemCondition> itemCondition;

    private final List<SlotRange> slotRanges;
    private final IntSet slots;

    private final Comparison comparison;
    private final int compareTo;

    public InventoryEntityConditionType(EnumSet<InventoryUtil.InventoryType> inventoryTypes, InventoryUtil.ProcessMode processMode, Optional<PowerReference> power, Optional<ItemCondition> itemCondition, List<SlotRange> slotRanges, Comparison comparison, int compareTo) {

        this.inventoryTypes = inventoryTypes;
        this.processMode = processMode;

        this.power = power;
        this.itemCondition = itemCondition;

        this.slotRanges = slotRanges;
        this.slots = MiscUtil.toSlotIdSet(slotRanges);

        this.comparison = comparison;
        this.compareTo = compareTo;

    }

    @Override
    public boolean test(EntityConditionContext context) {

        Entity entity = context.entity();
        int matches = 0;

        if (inventoryTypes.contains(InventoryUtil.InventoryType.INVENTORY)) {
            matches += InventoryUtil.checkInventory(entity, slots, Optional.empty(), itemCondition, processMode);
        }

        if (inventoryTypes.contains(InventoryUtil.InventoryType.POWER)) {

            Optional<InventoryPowerType> inventoryPowerType = power
                .map(p -> p.getNullablePowerType(entity))
                .filter(InventoryPowerType.class::isInstance)
                .map(InventoryPowerType.class::cast);

            matches += InventoryUtil.checkInventory(entity, slots, inventoryPowerType, itemCondition, processMode);

        }

        return comparison.compare(matches, compareTo);

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.INVENTORY;
    }

}
