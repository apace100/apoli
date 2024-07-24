package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class EnchantmentCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        RegistryKey<Enchantment> enchantmentKey = data.get("enchantment");
        RegistryEntry<Enchantment> enchantment = enchantmentKey == null ? null : worldAndStack.getLeft().getRegistryManager().get(RegistryKeys.ENCHANTMENT)
            .getEntry(enchantmentKey)
            .orElseThrow();

        Comparison comparison = data.get("comparison");
        int compareTo = data.get("compare_to");

        boolean useModifications = data.get("use_modifications");
        ItemEnchantmentsComponent component = ModifyEnchantmentLevelPower.getEnchantments(worldAndStack.getRight(), worldAndStack.getRight().getEnchantments(), useModifications);
        int level = enchantment != null ? component.getLevel(enchantment)
                                        : component.getEnchantments().size();
        return comparison.compare(level, compareTo);

    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("enchantment"),
            new SerializableData()
                .add("enchantment", SerializableDataTypes.ENCHANTMENT, null)
                .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
                .add("compare_to", SerializableDataTypes.INT, 0)
                .add("use_modifications", SerializableDataTypes.BOOLEAN, true),
            EnchantmentCondition::condition
        );
    }

}
