package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.InventoryPowerType;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import io.github.apace100.calio.util.ArgumentWrapper;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

//  TODO: Refactor this to follow the format of other condition types -eggohito
public class InventoryEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<InventoryEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("inventory_types", ApoliDataTypes.INVENTORY_TYPE_SET, EnumSet.allOf(InventoryUtil.InventoryType.class))
            .add("process_mode", ApoliDataTypes.PROCESS_MODE, InventoryUtil.ProcessMode.ITEMS)
            .add("power", ApoliDataTypes.POWER_REFERENCE.optional(), Optional.empty())
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("slots", ApoliDataTypes.ITEM_SLOTS.optional(), Optional.empty())
            .add("slot", ApoliDataTypes.ITEM_SLOT.optional(), Optional.empty())
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
            .add("compare_to", SerializableDataTypes.INT, 0),
        data -> new InventoryEntityConditionType(
            data.get("inventory_types"),
            data.get("process_mode"),
            data.get("power"),
            data.get("item_condition"),
            data.get("slots"),
            data.get("slot"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("inventory_types", conditionType.inventoryTypes)
            .set("process_mode", conditionType.processMode)
            .set("power", conditionType.power)
            .set("item_condition", conditionType.itemCondition)
            .set("slots", conditionType.slots)
            .set("slot", conditionType.slot)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final EnumSet<InventoryUtil.InventoryType> inventoryTypes;
    private final InventoryUtil.ProcessMode processMode;

    private final Optional<PowerReference> power;
    private final Optional<ItemCondition> itemCondition;

    private final Optional<List<ArgumentWrapper<Integer>>> slots;
    private final Optional<ArgumentWrapper<Integer>> slot;

    private final Set<Integer> unwrappedSlots;

    private final Comparison comparison;
    private final int compareTo;

    public InventoryEntityConditionType(EnumSet<InventoryUtil.InventoryType> inventoryTypes, InventoryUtil.ProcessMode processMode, Optional<PowerReference> power, Optional<ItemCondition> itemCondition, Optional<List<ArgumentWrapper<Integer>>> slots, Optional<ArgumentWrapper<Integer>> slot, Comparison comparison, int compareTo) {

        this.inventoryTypes = inventoryTypes;
        this.processMode = processMode;

        this.power = power;
        this.itemCondition = AbstractCondition.setPowerType(itemCondition, getPowerType());

        this.slots = slots;
        this.slot = slot;

        this.unwrappedSlots = new ObjectOpenHashSet<>();

        this.slots.ifPresent(wrappedSlots -> wrappedSlots.stream().map(ArgumentWrapper::argument).forEach(this.unwrappedSlots::add));
        this.slot.ifPresent(wrappedSlot -> this.unwrappedSlots.add(wrappedSlot.argument()));

        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(Entity entity) {

        int matches = 0;
        if (inventoryTypes.contains(InventoryUtil.InventoryType.INVENTORY)) {
            matches += InventoryUtil.checkInventory(entity, unwrappedSlots, Optional.empty(), itemCondition, processMode);
        }

        if (inventoryTypes.contains(InventoryUtil.InventoryType.POWER)) {

            Optional<InventoryPowerType> inventoryPowerType = power
                .map(p -> p.getType(entity))
                .filter(InventoryPowerType.class::isInstance)
                .map(InventoryPowerType.class::cast);

            matches += InventoryUtil.checkInventory(entity, unwrappedSlots, inventoryPowerType, itemCondition, processMode);

        }

        return comparison.compare(matches, compareTo);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.INVENTORY;
    }

}
