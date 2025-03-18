package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.InventoryPowerType;
import io.github.apace100.apoli.util.InventoryUtil.InventoryType;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.SlotRange;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static io.github.apace100.apoli.util.InventoryUtil.replaceInventory;

public class ReplaceInventoryEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ReplaceInventoryEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("inventory_type", ApoliDataTypes.INVENTORY_TYPE, InventoryType.INVENTORY)
            .add("power", ApoliDataTypes.POWER_REFERENCE.optional(), Optional.empty())
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("stack", SerializableDataTypes.ITEM_STACK)
            .add("slot", ApoliDataTypes.SLOT_RANGE, null)
            .addFunctionedDefault("slots", ApoliDataTypes.SLOT_RANGES, data -> MiscUtil.singletonListOrEmpty(data.get("slot")))
            .add("merge_nbt", SerializableDataTypes.BOOLEAN, false),
        data -> new ReplaceInventoryEntityActionType(
            data.get("inventory_type"),
            data.get("power"),
            data.get("entity_action"),
            data.get("item_action"),
            data.get("item_condition"),
            data.get("stack"),
            data.get("slots"),
            data.get("merge_nbt")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("inventory_type", actionType.inventoryType)
            .set("power", actionType.power)
            .set("entity_action", actionType.entityAction)
            .set("item_action", actionType.itemAction)
            .set("item_condition", actionType.itemCondition)
            .set("stack", actionType.stack)
            .set("slots", actionType.slotRanges)
            .set("merge_nbt", actionType.mergeNbt)
    );

    private final InventoryType inventoryType;
    private final Optional<PowerReference> power;

    private final Optional<EntityAction> entityAction;
    private final Optional<ItemAction> itemAction;

    private final Optional<ItemCondition> itemCondition;
    private final ItemStack stack;

    private final List<SlotRange> slotRanges;
    private final IntSet slots;

    private final boolean mergeNbt;

    public ReplaceInventoryEntityActionType(InventoryType inventoryType, Optional<PowerReference> power, Optional<EntityAction> entityAction, Optional<ItemAction> itemAction, Optional<ItemCondition> itemCondition, ItemStack stack, List<SlotRange> slotRanges, boolean mergeNbt) {

        this.inventoryType = inventoryType;
        this.power = power;

        this.entityAction = entityAction;
        this.itemAction = itemAction;

        this.itemCondition = itemCondition;
        this.stack = stack;

        this.slotRanges = slotRanges;
        this.slots = MiscUtil.toSlotIdSet(slotRanges);

        this.mergeNbt = mergeNbt;

    }

    @Override
    public void accept(EntityActionContext context) {

        Entity entity = context.entity();
        Optional<InventoryPowerType> inventoryPowerType = power
            .filter(p -> inventoryType == InventoryType.POWER)
            .flatMap(p -> p.getOptionalPowerType(entity))
            .filter(InventoryPowerType.class::isInstance)
            .map(InventoryPowerType.class::cast);

        replaceInventory(entity, slots, inventoryPowerType, entityAction, itemAction, itemCondition, stack, mergeNbt);

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.REPLACE_INVENTORY;
    }

}
