package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.action.factory.ItemActionTypeFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.LinkedList;

public class ModifyItemCooldownActionType {

    public static void action(ItemStack stack, Collection<Modifier> modifiers) {

        if (stack.isEmpty() || modifiers.isEmpty() || !(((EntityLinkedItemStack) stack).apoli$getEntity(true) instanceof PlayerEntity player)) {
            return;
        }

        ItemCooldownManager cooldownManager = player.getItemCooldownManager();
        ItemCooldownManager.Entry cooldownEntry = cooldownManager.entries.get(stack.getItem());

        int oldDuration = cooldownEntry != null
            ? cooldownEntry.endTick - cooldownEntry.startTick
            : 0;

        cooldownManager.set(stack.getItem(), (int) ModifierUtil.applyModifiers(player, modifiers, oldDuration));

    }

    public static ActionTypeFactory<Pair<World, StackReference>> getFactory() {
        return ItemActionTypeFactory.createItemStackBased(
            Apoli.identifier("modify_item_cooldown"),
            new SerializableData()
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            (data, worldAndStack) -> {

                Collection<Modifier> modifiers = new LinkedList<>();

                data.ifPresent("modifier", modifiers::add);
                data.ifPresent("modifiers", modifiers::addAll);

                action(worldAndStack.getRight(), modifiers);

            }
        );
    }

}
