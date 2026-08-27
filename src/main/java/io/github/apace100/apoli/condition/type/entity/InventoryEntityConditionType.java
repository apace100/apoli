package io.github.apace100.apoli.condition.type.entity;

import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SlotRange;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class InventoryEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<InventoryEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("inventory_types", ApoliDataTypes.INVENTORY_TYPE_SET, EnumSet.allOf(InventoryUtil.InventoryType.class))
            .add("process_mode", ApoliDataTypes.PROCESS_MODE, InventoryUtil.ProcessMode.ITEMS)
            .add("power", ApoliDataTypes.POWER_REFERENCE.optional(), Optional.empty())
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("slot", ApoliDataTypes.SLOT_RANGE, null)
            .addFunctionedDefault("slots", ApoliDataTypes.SLOT_RANGES, data -> InventoryUtil.singleOrAllSlots(data.getOptional("slot")))
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
            matches += this.countMatches(entity, Either.right(entity));
        }

        if (inventoryTypes.contains(InventoryUtil.InventoryType.POWER)) {

            for (var inventory : InventoryUtil.getPowerInventories(entity, power.orElse(null))) {
                matches += this.countMatches(entity, Either.left(inventory));
            }

        }

        return comparison.compare(matches, compareTo);

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return EntityConditionTypes.INVENTORY;
    }

    private int countMatches(Entity entity, Either<Inventory, Entity> source) {

        OptionalInt slotToSkip = InventoryUtil.getSelectedHotBarSlot(entity);
        int matches = 0;

        for (int slot : slots) {

            if (slotToSkip.isPresent() && slotToSkip.getAsInt() == slot) {
                continue;
            }

            StackReference reference = InventoryUtil.getStackReference(source, slot);
            ItemStack stack = reference.get();

            if (reference != StackReference.EMPTY && itemCondition.map(condition -> condition.test(entity.getWorld(), stack)).orElse(false)) {
                matches += processMode.applyAsInt(stack);
            }

        }

        return matches;

    }

}
