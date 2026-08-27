package io.github.apace100.apoli.action.type.entity;

import com.mojang.datafixers.util.Either;
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
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.InventoryUtil.InventoryType;
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

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class DropInventoryEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<DropInventoryEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("inventory_type", ApoliDataTypes.INVENTORY_TYPE, InventoryType.INVENTORY)
            .add("power", ApoliDataTypes.POWER_REFERENCE.optional(), Optional.empty())
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("slot", ApoliDataTypes.SLOT_RANGE, null)
            .addFunctionedDefault("slots", ApoliDataTypes.SLOT_RANGES, data -> InventoryUtil.singleOrAllSlots(data.getOptional("slot")))
            .add("throw_randomly", SerializableDataTypes.BOOLEAN, false)
            .add("retain_ownership", SerializableDataTypes.BOOLEAN, false)
            .add("amount", SerializableDataTypes.POSITIVE_INT.optional(), Optional.empty()),
        data -> new DropInventoryEntityActionType(
            data.get("inventory_type"),
            data.get("power"),
            data.get("entity_action"),
            data.get("item_action"),
            data.get("item_condition"),
			data.get("slots"),
            data.get("throw_randomly"),
            data.get("retain_ownership"),
            data.get("amount")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("inventory_type", actionType.inventoryType)
            .set("power", actionType.power)
            .set("entity_action", actionType.entityAction)
            .set("item_action", actionType.itemAction)
            .set("item_condition", actionType.itemCondition)
            .set("slots", actionType.slotRanges)
            .set("throw_randomly", actionType.throwRandomly)
            .set("retain_ownership", actionType.retainOwnership)
            .set("amount", actionType.amount)
    );

    private final InventoryType inventoryType;
    private final Optional<PowerReference> power;

    private final Optional<EntityAction> entityAction;

    private final Optional<ItemAction> itemAction;
    private final Optional<ItemCondition> itemCondition;

    private final List<SlotRange> slotRanges;
    private final IntSet slots;

    private final boolean throwRandomly;
    private final boolean retainOwnership;

    private final Optional<Integer> amount;

    public DropInventoryEntityActionType(InventoryType inventoryType, Optional<PowerReference> power, Optional<EntityAction> entityAction, Optional<ItemAction> itemAction, Optional<ItemCondition> itemCondition, List<SlotRange> slotRanges, boolean throwRandomly, boolean retainOwnership, Optional<Integer> amount) {

        this.inventoryType = inventoryType;
        this.power = power;

        this.entityAction = entityAction;
        this.itemAction = itemAction;
        this.itemCondition = itemCondition;

        this.slotRanges = slotRanges;
        this.slots = MiscUtil.toSlotIdSet(slotRanges);

        this.throwRandomly = throwRandomly;
        this.retainOwnership = retainOwnership;
        this.amount = amount;

    }

    @Override
    public void accept(EntityActionContext context) {

        if (context.world().isClient()) {
            return;
        }

        Entity entity = context.entity();

        switch (inventoryType) {
            case INVENTORY ->
                this.dropMatches(entity, Either.right(entity));
            case POWER -> {

                for (var inventory : InventoryUtil.getPowerInventories(entity, power.orElse(null))) {
                    this.dropMatches(entity, Either.left(inventory));
                }

            }
        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.DROP_INVENTORY;
    }

    private void dropMatches(Entity thrower, Either<Inventory, Entity> source) {

        OptionalInt slotToSkip = InventoryUtil.getSelectedHotBarSlot(thrower);

        for (int slot : slots) {

            if (slotToSkip.isPresent() && slotToSkip.getAsInt() == slot) {
                continue;
            }

            StackReference reference = InventoryUtil.getStackReference(source, slot);
            ItemStack stack = reference.get();

            if (reference == StackReference.EMPTY || !itemCondition.map(condition -> condition.test(thrower.getWorld(), stack)).orElse(true)) {
                continue;
            }

            entityAction.ifPresent(action -> action.execute(thrower));
            itemAction.ifPresent(action -> action.execute(thrower.getWorld(), reference));

            Optional<ItemStack> splitStack = amount
                .map(Math::abs)
                .map(stack::split);

            InventoryUtil.throwItem(thrower, splitStack.orElse(stack), throwRandomly, retainOwnership);

            if (splitStack.isEmpty()) {
                reference.set(ItemStack.EMPTY);
            }

        }

    }

}
