package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

public class EnchantmentCondition {

    public static boolean condition(SerializableData.Instance data, ItemStack stack) {

        Enchantment enchantment = data.get("enchantment");
        Comparison comparison = data.get("comparison");
        int compareTo = data.get("compare_to");

        if (enchantment != null) return comparison.compare(ModifyEnchantmentLevelPower.getLevel(enchantment, stack), compareTo);
        else return comparison.compare(ModifyEnchantmentLevelPower.get(stack).size(), compareTo);

    }

    public static ConditionFactory<ItemStack> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("enchantment"),
            new SerializableData()
                .add("enchantment", SerializableDataTypes.ENCHANTMENT, null)
                .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
                .add("compare_to", SerializableDataTypes.INT, 0),
            EnchantmentCondition::condition
        );
    }

}
