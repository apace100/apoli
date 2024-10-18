package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public class GiveEntityActionType extends EntityActionType {

    public static final DataObjectFactory<GiveEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("preferred_slot", SerializableDataTypes.ATTRIBUTE_MODIFIER_SLOT.optional(), Optional.empty())
            .add("stack", SerializableDataTypes.ITEM_STACK),
        data -> new GiveEntityActionType(
            data.get("item_action"),
            data.get("preferred_slot"),
            data.get("stack")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("item_action", actionType.itemAction)
            .set("preferred_slot", actionType.preferredSlot)
            .set("stack", actionType.stack)
    );

    private final Optional<ItemAction> itemAction;

    private final Optional<AttributeModifierSlot> preferredSlot;
    private final ItemStack stack;

    public GiveEntityActionType(Optional<ItemAction> itemAction, Optional<AttributeModifierSlot> preferredSlot, ItemStack stack) {
        this.itemAction = itemAction;
        this.preferredSlot = preferredSlot;
        this.stack = stack;
    }

    @Override
    protected void execute(Entity entity) {

        if (entity.getWorld().isClient() || stack.isEmpty()) {
            return;
        }

        StackReference stackReference = InventoryUtil.createStackReference(stack);
        itemAction.ifPresent(action -> action.execute(entity.getWorld(), stackReference));

        ItemStack stackToGive = stackReference.get();

        if (preferredSlot.isPresent() && entity instanceof LivingEntity living) {

            AttributeModifierSlot actualPreferredSlot = preferredSlot.get();
            for (EquipmentSlot slot : EquipmentSlot.values()) {

                if (!actualPreferredSlot.matches(slot)) {
                    continue;
                }

                ItemStack stackInSlot = living.getEquippedStack(slot);
                if (stackInSlot.isEmpty()) {
                    living.equipStack(slot, stackToGive);
                    return;
                }

                else if (ItemStack.areEqual(stackInSlot, stackToGive) && stackInSlot.getCount() < stackInSlot.getMaxCount()) {

                    int itemsToGive = Math.min(stackInSlot.getMaxCount() - stackInSlot.getCount(), stackToGive.getCount());

                    stackInSlot.increment(itemsToGive);
                    stackToGive.decrement(itemsToGive);

                    if (stackToGive.isEmpty()) {
                        return;
                    }

                }

            }

        }

        if (entity instanceof PlayerEntity player) {
            player.getInventory().offerOrDrop(stackToGive);
        }

        else {
            InventoryUtil.throwItem(entity, stackToGive, false , false);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.GIVE;
    }

}
