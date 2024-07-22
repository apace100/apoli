package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

public class EnchantmentCondition {

    public static boolean condition(Entity entity, RegistryKey<Enchantment> enchantmentKey, Calculation calculation, Comparison comparison, int compareTo, boolean useModifications) {

        Registry<Enchantment> enchantmentRegistry = entity.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        int enchantmentLevel = 0;

        if (entity instanceof LivingEntity livingEntity) {
            enchantmentLevel = calculation.queryTotalLevel(livingEntity, enchantmentRegistry.getEntry(enchantmentKey).orElseThrow(), useModifications);
        }

        return comparison.compare(enchantmentLevel, compareTo);

    }

    public enum Calculation {

        SUM {

            @Override
            public int queryLevel(ItemStack stack, RegistryEntry<Enchantment> enchantmentEntry, boolean useModifications, int totalLevel) {
                return ModifyEnchantmentLevelPower.getEnchantments(stack, stack.getEnchantments(), useModifications).getLevel(enchantmentEntry);
            }

        },

        MAX {

            @Override
            public int queryLevel(ItemStack stack, RegistryEntry<Enchantment> enchantmentEntry, boolean useModifications, int totalLevel) {

                int potentialLevel = ModifyEnchantmentLevelPower.getEnchantments(stack, stack.getEnchantments(), useModifications).getLevel(enchantmentEntry);

                if (potentialLevel >= totalLevel) {
                    return potentialLevel;
                }

                else {
                    return 0;
                }

            }

        };

        public int queryTotalLevel(LivingEntity entity, RegistryEntry<Enchantment> enchantmentEntry, boolean useModifications) {

            Enchantment enchantment = enchantmentEntry.value();
            int totalLevel = 0;

            for (ItemStack stack : enchantment.getEquipment(entity).values()) {
                totalLevel += this.queryLevel(stack, enchantmentEntry, useModifications, totalLevel);
            }

            return totalLevel;

        }

        public abstract int queryLevel(ItemStack stack, RegistryEntry<Enchantment> enchantmentEntry, boolean useModifications, int totalLevel);

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("enchantment"),
            new SerializableData()
                .add("enchantment", SerializableDataTypes.ENCHANTMENT)
                .add("calculation", SerializableDataType.enumValue(Calculation.class), Calculation.SUM)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT)
                .add("use_modifications", SerializableDataTypes.BOOLEAN, true),
            (data, entity) -> condition(
                entity,
                data.get("enchantment"),
                data.get("calculation"),
                data.get("comparison"),
                data.get("compare_to"),
                data.get("use_modifications")
            )
        );
    }

}
