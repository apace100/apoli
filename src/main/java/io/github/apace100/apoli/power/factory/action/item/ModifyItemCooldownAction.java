package io.github.apace100.apoli.power.factory.action.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;

public class ModifyItemCooldownAction {

    public static void action(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        ItemStack stack = worldAndStack.getRight();
        Entity stackHolder = ((EntityLinkedItemStack) stack).apoli$getEntity();

        if (stack.isEmpty() || !(stackHolder instanceof PlayerEntity player)) {
            return;
        }

        List<Modifier> modifiers = new LinkedList<>();

        data.<Modifier>ifPresent("modifier", modifiers::add);
        data.<List<Modifier>>ifPresent("modifiers", modifiers::addAll);

        Item item = stack.getItem();

        ItemCooldownManager cooldownManager = player.getItemCooldownManager();
        ItemCooldownManager.Entry cooldownEntry = cooldownManager.entries.get(item);

        int oldDuration = cooldownEntry != null ? cooldownEntry.endTick - cooldownEntry.startTick : 0;
        int newDuration = (int) ModifierUtil.applyModifiers(stackHolder, modifiers, oldDuration);

        cooldownManager.set(item, newDuration);

    }

    public static ActionFactory<Pair<World, StackReference>> getFactory() {
        return ItemActionFactory.createItemStackBased(
            Apoli.identifier("modify_item_cooldown"),
            new SerializableData()
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            ModifyItemCooldownAction::action
        );
    }

}
