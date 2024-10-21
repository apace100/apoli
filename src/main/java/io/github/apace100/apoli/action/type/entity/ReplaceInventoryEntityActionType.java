package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.InventoryPowerType;
import io.github.apace100.apoli.util.InventoryUtil.InventoryType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.ArgumentWrapper;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
            .add("slot", ApoliDataTypes.ITEM_SLOT.optional(), Optional.empty())
            .add("slots", ApoliDataTypes.ITEM_SLOTS.optional(), Optional.empty())
            .add("merge_nbt", SerializableDataTypes.BOOLEAN, false),
        data -> new ReplaceInventoryEntityActionType(
            data.get("inventory_type"),
            data.get("power"),
            data.get("entity_action"),
            data.get("item_action"),
            data.get("item_condition"),
            data.get("stack"),
            data.get("slot"),
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
            .set("slot", actionType.slot)
            .set("slots", actionType.slots)
            .set("merge_nbt", actionType.mergeNbt)
    );

    private final InventoryType inventoryType;
    private final Optional<PowerReference> power;

    private final Optional<EntityAction> entityAction;
    private final Optional<ItemAction> itemAction;

    private final Optional<ItemCondition> itemCondition;
    private final ItemStack stack;

    private final Optional<ArgumentWrapper<Integer>> slot;
    private final Optional<List<ArgumentWrapper<Integer>>> slots;

    private final Set<Integer> unwrappedSlots;
    private final boolean mergeNbt;

    public ReplaceInventoryEntityActionType(InventoryType inventoryType, Optional<PowerReference> power, Optional<EntityAction> entityAction, Optional<ItemAction> itemAction, Optional<ItemCondition> itemCondition, ItemStack stack, Optional<ArgumentWrapper<Integer>> slot, Optional<List<ArgumentWrapper<Integer>>> slots, boolean mergeNbt) {

        this.inventoryType = inventoryType;
        this.power = power;

        this.entityAction = entityAction;
        this.itemAction = itemAction;

        this.itemCondition = itemCondition;
        this.stack = stack;

        this.slot = slot;
        this.slots = slots;

        this.unwrappedSlots = new ObjectOpenHashSet<>();

        this.slot.map(ArgumentWrapper::argument).ifPresent(this.unwrappedSlots::add);
        this.slots.stream().flatMap(Collection::stream).map(ArgumentWrapper::argument).forEach(this.unwrappedSlots::add);

        this.mergeNbt = mergeNbt;

    }

    @Override
    protected void execute(Entity entity) {

        Optional<InventoryPowerType> inventoryPowerType = power
            .filter(p -> inventoryType == InventoryType.POWER)
            .map(p -> p.getType(entity))
            .filter(InventoryPowerType.class::isInstance)
            .map(InventoryPowerType.class::cast);

        replaceInventory(entity, unwrappedSlots, inventoryPowerType, entityAction, itemAction, itemCondition, stack, mergeNbt);

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.REPLACE_INVENTORY;
    }

}
