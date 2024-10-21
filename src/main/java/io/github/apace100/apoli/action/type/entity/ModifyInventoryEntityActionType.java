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
import io.github.apace100.apoli.util.InventoryUtil.ProcessMode;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.ArgumentWrapper;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.apace100.apoli.util.InventoryUtil.modifyInventory;

public class ModifyInventoryEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ModifyInventoryEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("inventory_type", ApoliDataTypes.INVENTORY_TYPE, InventoryType.INVENTORY)
            .add("process_mode", ApoliDataTypes.PROCESS_MODE, ProcessMode.STACKS)
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_action", ItemAction.DATA_TYPE)
            .add("power", ApoliDataTypes.POWER_REFERENCE.optional(), Optional.empty())
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("slot", ApoliDataTypes.ITEM_SLOT.optional(), Optional.empty())
            .add("slots", ApoliDataTypes.ITEM_SLOTS.optional(), Optional.empty())
            .add("limit", SerializableDataTypes.POSITIVE_INT.optional(), Optional.empty()),
        data -> new ModifyInventoryEntityActionType(
            data.get("inventory_type"),
            data.get("process_mode"),
            data.get("entity_action"),
            data.get("item_action"),
            data.get("power"),
            data.get("item_condition"),
            data.get("slot"),
            data.get("slots"),
            data.get("limit")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("inventory_type", actionType.inventoryType)
            .set("process_mode", actionType.processMode)
            .set("entity_action", actionType.entityAction)
            .set("item_action", actionType.itemAction)
            .set("power", actionType.power)
            .set("item_condition", actionType.itemCondition)
            .set("slot", actionType.slot)
            .set("slots", actionType.slots)
            .set("limit", actionType.limit)
    );

    private final InventoryType inventoryType;
    private final ProcessMode processMode;

    private final Optional<EntityAction> entityAction;
    private final ItemAction itemAction;

    private final Optional<PowerReference> power;
    private final Optional<ItemCondition> itemCondition;

    private final Optional<ArgumentWrapper<Integer>> slot;
    private final Optional<List<ArgumentWrapper<Integer>>> slots;

    private final Set<Integer> unwrappedSlots;

    private final Optional<Integer> limit;

    public ModifyInventoryEntityActionType(InventoryType inventoryType, ProcessMode processMode, Optional<EntityAction> entityAction, ItemAction itemAction, Optional<PowerReference> power, Optional<ItemCondition> itemCondition, Optional<ArgumentWrapper<Integer>> slot, Optional<List<ArgumentWrapper<Integer>>> slots, Optional<Integer> limit) {

        this.inventoryType = inventoryType;
        this.processMode = processMode;

        this.entityAction = entityAction;
        this.itemAction = itemAction;

        this.power = power;
        this.itemCondition = itemCondition;

        this.slot = slot;
        this.slots = slots;

        this.unwrappedSlots = new ObjectOpenHashSet<>();

        this.slot.map(ArgumentWrapper::argument).ifPresent(this.unwrappedSlots::add);
        this.slots.stream().flatMap(Collection::stream).map(ArgumentWrapper::argument).forEach(this.unwrappedSlots::add);

        this.limit = limit;

    }

    @Override
    protected void execute(Entity entity) {

        Optional<InventoryPowerType> inventoryPowerType = power
            .filter(ipt -> inventoryType == InventoryType.POWER)
            .map(p -> p.getType(entity))
            .filter(InventoryPowerType.class::isInstance)
            .map(InventoryPowerType.class::cast);

        modifyInventory(entity, unwrappedSlots, inventoryPowerType, entityAction, itemAction, itemCondition, limit, processMode);

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.MODIFY_INVENTORY;
    }

}
