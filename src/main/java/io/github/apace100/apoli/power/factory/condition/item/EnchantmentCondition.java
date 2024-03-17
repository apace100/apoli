package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.StackPowerUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class EnchantmentCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        Enchantment enchantment = data.get("enchantment");
        Comparison comparison = data.get("comparison");

        boolean useModifications = data.get("use_modifications");
        int compareTo = data.get("compare_to");

        int level = enchantment != null ? ModifyEnchantmentLevelPower.getLevel(enchantment, worldAndStack.getRight(), useModifications)
                                        : ModifyEnchantmentLevelPower.get(worldAndStack.getRight(), useModifications).size();

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
