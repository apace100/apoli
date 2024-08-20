package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.action.factory.ItemActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

public class RemoveEnchantmentActionType {

    public static void action(DynamicRegistryManager registryManager, ItemStack stack, Collection<RegistryKey<Enchantment>> enchantmentKeys, @Nullable Integer levels, boolean resetRepairCost) {

        if (!stack.hasEnchantments()) {
            return;
        }

        ItemEnchantmentsComponent oldEnchantments = stack.getEnchantments();
        ItemEnchantmentsComponent.Builder newEnchantments = new ItemEnchantmentsComponent.Builder(oldEnchantments);

        boolean hasKeys = false;
        for (RegistryKey<Enchantment> enchantmentKey : enchantmentKeys) {

            //  Since the registry keys are already validated, this should be fine.
            RegistryEntry<Enchantment> enchantment = registryManager.get(RegistryKeys.ENCHANTMENT).entryOf(enchantmentKey);
            hasKeys = true;

            if (oldEnchantments.getEnchantments().contains(enchantment)) {
                newEnchantments.set(enchantment, levels != null ? oldEnchantments.getLevel(enchantment) - levels : 0);
            }

        }

        for (RegistryEntry<Enchantment> oldEnchantment : oldEnchantments.getEnchantments()) {

            if (hasKeys) {
                break;
            }

            else {
                newEnchantments.set(oldEnchantment, levels != null ? oldEnchantments.getLevel(oldEnchantment) - levels : 0);
            }

        }

        stack.set(DataComponentTypes.ENCHANTMENTS, newEnchantments.build());
        if (resetRepairCost && !stack.hasEnchantments()) {
            stack.set(DataComponentTypes.REPAIR_COST, 0);
        }

    }

    public static ActionTypeFactory<Pair<World, StackReference>> getFactory() {
        return ItemActionTypeFactory.createItemStackBased(
            Apoli.identifier("remove_enchantment"),
            new SerializableData()
                .add("enchantment", SerializableDataTypes.ENCHANTMENT, null)
                .add("enchantments", SerializableDataTypes.ENCHANTMENT.listOf(), null)
                .add("levels", SerializableDataTypes.INT, null)
                .add("reset_repair_cost", SerializableDataTypes.BOOLEAN, false),
            (data, worldAndStack) -> {

                Collection<RegistryKey<Enchantment>> enchantmentKeys = new HashSet<>();

                data.ifPresent("enchantment", enchantmentKeys::add);
                data.ifPresent("enchantments", enchantmentKeys::addAll);

                action(worldAndStack.getLeft().getRegistryManager(), worldAndStack.getRight(),
                    enchantmentKeys,
                    data.get("levels"),
                    data.get("reset_repair_cost")
                );

            }
        );
    }

}
