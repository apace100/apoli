package io.github.apace100.apoli.power.factory.action.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;

public class RemoveEnchantmentAction {

    public static void action(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        ItemStack stack = worldAndStack.getRight();
        World world = worldAndStack.getLeft();

        if (!stack.hasEnchantments()) {
            return;
        }

        List<RegistryKey<Enchantment>> enchantmentKeys = new LinkedList<>();

        data.<RegistryKey<Enchantment>>ifPresent("enchantment", enchantmentKeys::add);
        data.<List<RegistryKey<Enchantment>>>ifPresent("enchantments", enchantmentKeys::addAll);

        ItemEnchantmentsComponent component = stack.getEnchantments();
        ItemEnchantmentsComponent.Builder componentBuilder = new ItemEnchantmentsComponent.Builder(component);

        Integer levels = data.isPresent("levels")
            ? data.getInt("levels")
            : null;

        if (!enchantmentKeys.isEmpty()) {

            for (RegistryKey<Enchantment> enchantmentKey : enchantmentKeys) {

                //  Since the registry keys are already validated by the data type, this should be fine... right...?
                RegistryEntry<Enchantment> enchantmentEntry = world.getRegistryManager()
                    .get(RegistryKeys.ENCHANTMENT)
                    .getEntry(enchantmentKey)
                    .orElseThrow();

                if (component.getEnchantments().contains(enchantmentEntry)) {
                    componentBuilder.set(enchantmentEntry, levels != null ? component.getLevel(enchantmentEntry) - levels : 0);
                }

            }

        }

        else {

            for (RegistryEntry<Enchantment> enchantment : component.getEnchantments()) {
                componentBuilder.set(enchantment, levels != null ? component.getLevel(enchantment) - levels : 0);
            }

        }

        stack.set(DataComponentTypes.ENCHANTMENTS, componentBuilder.build());
        if (data.getBoolean("reset_repair_cost") && !stack.hasEnchantments()) {
            stack.set(DataComponentTypes.REPAIR_COST, 0);
        }

    }

    public static ActionFactory<Pair<World, StackReference>> getFactory() {
        return ItemActionFactory.createItemStackBased(
            Apoli.identifier("remove_enchantment"),
            new SerializableData()
                .add("enchantment", SerializableDataTypes.ENCHANTMENT, null)
                .add("enchantments", SerializableDataTypes.ENCHANTMENT.listOf(), null)
                .add("levels", SerializableDataTypes.INT, null)
                .add("reset_repair_cost", SerializableDataTypes.BOOLEAN, false),
            RemoveEnchantmentAction::action
        );
    }

}
