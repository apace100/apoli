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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.apace100.apoli.util.InventoryUtil.dropInventory;

public class DropInventoryEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<DropInventoryEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("inventory_type", ApoliDataTypes.INVENTORY_TYPE, InventoryType.INVENTORY)
            .add("power", ApoliDataTypes.POWER_REFERENCE.optional(), Optional.empty())
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("slot", ApoliDataTypes.ITEM_SLOT.optional(), Optional.empty())
            .add("slots", ApoliDataTypes.ITEM_SLOTS.optional(), Optional.empty())
            .add("throw_randomly", SerializableDataTypes.BOOLEAN, false)
            .add("retain_ownership", SerializableDataTypes.BOOLEAN, false)
            .add("amount", SerializableDataTypes.POSITIVE_INT.optional(), Optional.empty()),
        data -> new DropInventoryEntityActionType(
            data.get("inventory_type"),
            data.get("power"),
            data.get("entity_action"),
            data.get("item_action"),
            data.get("item_condition"),
            data.get("slot"),
            data.get("slots"),
            data.get("throw_randomly"),
            data.get("retain_ownership"),
            data.get("amount")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("inventory_type", actionType.inventoryType)
            .set("power", actionType.inventoryType)
            .set("entity_action", actionType.entityAction)
            .set("item_action", actionType.itemAction)
            .set("item_condition", actionType.itemCondition)
            .set("slot", actionType.slot)
            .set("slots", actionType.slots)
            .set("throw_randomly", actionType.throwRandomly)
            .set("retain_ownership", actionType.retainOwnership)
            .set("amount", actionType.amount)
    );

    private final InventoryType inventoryType;
    private final Optional<PowerReference> power;

    private final Optional<EntityAction> entityAction;

    private final Optional<ItemAction> itemAction;
    private final Optional<ItemCondition> itemCondition;

    private final Optional<ArgumentWrapper<Integer>> slot;
    private final Optional<List<ArgumentWrapper<Integer>>> slots;

    private final Set<Integer> unwrappedSlots;

    private final boolean throwRandomly;
    private final boolean retainOwnership;

    private final Optional<Integer> amount;

    public DropInventoryEntityActionType(InventoryType inventoryType, Optional<PowerReference> power, Optional<EntityAction> entityAction, Optional<ItemAction> itemAction, Optional<ItemCondition> itemCondition, Optional<ArgumentWrapper<Integer>> slot, Optional<List<ArgumentWrapper<Integer>>> slots, boolean throwRandomly, boolean retainOwnership, Optional<Integer> amount) {

        this.inventoryType = inventoryType;
        this.power = power;

        this.entityAction = entityAction;
        this.itemAction = itemAction;
        this.itemCondition = itemCondition;

        this.slot = slot;
        this.slots = slots;

        this.unwrappedSlots = new ObjectOpenHashSet<>();

        this.slot.map(ArgumentWrapper::argument).ifPresent(this.unwrappedSlots::add);
        this.slots.stream().flatMap(Collection::stream).map(ArgumentWrapper::argument).forEach(this.unwrappedSlots::add);

        this.throwRandomly = throwRandomly;
        this.retainOwnership = retainOwnership;
        this.amount = amount;

    }

    @Override
    protected void execute(Entity entity) {

        Optional<InventoryPowerType> inventoryPowerType = power
            .filter(ivp -> inventoryType == InventoryType.POWER)
            .map(p -> p.getType(entity))
            .filter(InventoryPowerType.class::isInstance)
            .map(InventoryPowerType.class::cast);

        dropInventory(entity, unwrappedSlots, inventoryPowerType, entityAction, itemAction, itemCondition, throwRandomly, retainOwnership, amount);

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.DROP_INVENTORY;
    }

}
