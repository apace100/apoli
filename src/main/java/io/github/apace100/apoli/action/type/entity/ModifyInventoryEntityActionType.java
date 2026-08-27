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
import io.github.apace100.apoli.util.InventoryUtil.ProcessMode;
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

public class ModifyInventoryEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ModifyInventoryEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("inventory_type", ApoliDataTypes.INVENTORY_TYPE, InventoryType.INVENTORY)
            .add("process_mode", ApoliDataTypes.PROCESS_MODE, ProcessMode.STACKS)
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_action", ItemAction.DATA_TYPE)
            .add("power", ApoliDataTypes.POWER_REFERENCE.optional(), Optional.empty())
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("slot", ApoliDataTypes.SLOT_RANGE, null)
            .addFunctionedDefault("slots", ApoliDataTypes.SLOT_RANGES, data -> InventoryUtil.singleOrAllSlots(data.getOptional("slot")))
            .add("limit", SerializableDataTypes.POSITIVE_INT, Integer.MAX_VALUE),
        data -> new ModifyInventoryEntityActionType(
            data.get("inventory_type"),
            data.get("process_mode"),
            data.get("entity_action"),
            data.get("item_action"),
            data.get("power"),
            data.get("item_condition"),
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
            .set("slots", actionType.slotRanges)
            .set("limit", actionType.limit)
    );

    private final InventoryType inventoryType;
    private final ProcessMode processMode;

    private final Optional<EntityAction> entityAction;
    private final ItemAction itemAction;

    private final Optional<PowerReference> power;
    private final Optional<ItemCondition> itemCondition;

    private final List<SlotRange> slotRanges;
    private final IntSet slots;

    private final int limit;

    public ModifyInventoryEntityActionType(InventoryType inventoryType, ProcessMode processMode, Optional<EntityAction> entityAction, ItemAction itemAction, Optional<PowerReference> power, Optional<ItemCondition> itemCondition, List<SlotRange> slotRanges, int limit) {

        this.inventoryType = inventoryType;
        this.processMode = processMode;

        this.entityAction = entityAction;
        this.itemAction = itemAction;

        this.power = power;
        this.itemCondition = itemCondition;

        this.slotRanges = slotRanges;
        this.slots = MiscUtil.toSlotIdSet(slotRanges);

        this.limit = limit;

    }

    @Override
    public void accept(EntityActionContext context) {

        if (context.world().isClient()) {
            return;
        }

        Entity entity = context.entity();

        switch (inventoryType) {
            case INVENTORY ->
                this.modifyMatches(entity, Either.right(entity));
            case POWER -> {

                for (var inventory : InventoryUtil.getPowerInventories(entity, power.orElse(null))) {
                    this.modifyMatches(entity, Either.left(inventory));
                }

            }
        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.MODIFY_INVENTORY;
    }

    private void modifyMatches(Entity thrower, Either<Inventory, Entity> source) {

        OptionalInt slotToSkip = InventoryUtil.getSelectedHotBarSlot(thrower);
        int processed = 0;

        for (int slot : slots) {

            if (slotToSkip.isPresent() && slotToSkip.getAsInt() == slot) {
                continue;
            }

            StackReference reference = InventoryUtil.getStackReference(source, slot);
            ItemStack stack = reference.get();

            if (reference == StackReference.EMPTY || !itemCondition.map(condition -> condition.test(thrower.getWorld(), stack)).orElse(true)) {
                continue;
            }

            int amount = processMode.applyAsInt(stack);

            for (int i = 0; i < amount && !stack.isEmpty(); i++) {

                entityAction.ifPresent(action -> action.execute(thrower));

	            switch (processMode) {
                    //  TODO:   Figure out how to insert the item back to the entity's inventory since stack references
                    //          do not have a reference to its backing inventory
                    case ITEMS -> {

			            StackReference copyReference = InventoryUtil.createStackReference(stack.copyWithCount(1));
			            itemAction.execute(thrower.getWorld(), copyReference);

			            if (!ItemStack.areItemsAndComponentsEqual(stack, copyReference.get())) {

				            InventoryUtil.throwItem(thrower, copyReference.get(), false, false, 0);
				            stack.decrement(1);

                            processed++;

			            }

		            }
		            case STACKS -> {
                        itemAction.execute(thrower.getWorld(), reference);
                        processed++;
                    }
	            }

                if (processed >= limit) {
                    return;
                }

            }

        }

    }

}
