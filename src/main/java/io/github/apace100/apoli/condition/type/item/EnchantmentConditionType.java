package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.type.ModifyEnchantmentLevelPowerType;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnchantmentConditionType {

    public static boolean condition(DynamicRegistryManager registryManager, ItemStack stack, @Nullable RegistryKey<Enchantment> enchantmentKey, Comparison comparison, int compareTo, boolean useModifications) {

        RegistryEntry<Enchantment> enchantment = enchantmentKey != null
            ? registryManager.get(RegistryKeys.ENCHANTMENT).entryOf(enchantmentKey)
            : null;

        ItemEnchantmentsComponent enchantmentsComponent = ModifyEnchantmentLevelPowerType.getEnchantments(stack, stack.getEnchantments(), useModifications);
        int level = enchantment != null
            ? enchantmentsComponent.getLevel(enchantment)
            : enchantmentsComponent.getSize();

        return comparison.compare(level, compareTo);

    }

    public static ConditionTypeFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("enchantment"),
            new SerializableData()
                .add("enchantment", SerializableDataTypes.ENCHANTMENT, null)
                .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
                .add("compare_to", SerializableDataTypes.INT, 0)
                .add("use_modifications", SerializableDataTypes.BOOLEAN, true),
            (data, worldAndStack) -> condition(worldAndStack.getLeft().getRegistryManager(), worldAndStack.getRight(),
                data.get("enchantment"),
                data.get("comparison"),
                data.get("compare_to"),
                data.get("use_modifications")
            )
        );
    }

}
