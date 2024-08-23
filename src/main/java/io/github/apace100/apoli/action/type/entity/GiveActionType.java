package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 *  TODO: Use {@link SerializableDataTypes#ATTRIBUTE_MODIFIER_SLOT} for the {@code preferred_slot} field -eggohito
 */
public class GiveActionType {

    public static void action(Entity entity, ItemStack newStack, Consumer<Pair<World, StackReference>> itemAction, @Nullable EquipmentSlot preferredSlot) {

        if (entity.getWorld().isClient || newStack.isEmpty()) {
            return;
        }

        StackReference stackReference = InventoryUtil.createStackReference(newStack);
        itemAction.accept(new Pair<>(entity.getWorld(), stackReference));

        ItemStack stackToGive = stackReference.get();

        tryPreferredSlot:
        if (preferredSlot != null && entity instanceof LivingEntity living) {

            ItemStack stackInSlot = living.getEquippedStack(preferredSlot);
            if (stackInSlot.isEmpty()) {
                living.equipStack(preferredSlot, stackToGive);
                return;
            }

            if (!ItemStack.areEqual(stackInSlot, stackToGive) || stackInSlot.getCount() >= stackInSlot.getMaxCount()) {
                break tryPreferredSlot;
            }

            int itemsToGive = Math.min(stackInSlot.getMaxCount() - stackInSlot.getCount(), stackToGive.getCount());

            stackInSlot.increment(itemsToGive);
            stackToGive.decrement(itemsToGive);

            if (stackToGive.isEmpty()) {
                return;
            }

        }

        if (entity instanceof PlayerEntity player) {
            player.getInventory().offerOrDrop(stackToGive);
        }

        else {
            InventoryUtil.throwItem(entity, stackToGive, false , false);
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("give"),
            new SerializableData()
                .add("stack", SerializableDataTypes.ITEM_STACK)
                .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("preferred_slot", SerializableDataTypes.EQUIPMENT_SLOT, null),
            (data, entity) -> action(entity,
                data.<ItemStack>get("stack").copy(),
                data.getOrElse("item_action", wsr -> {}),
                data.get("preferred_slot")
            )
        );
    }

}
