package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.type.ModifyEnchantmentLevelPowerType;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

public class EnchantmentEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<EnchantmentEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("enchantment", SerializableDataTypes.ENCHANTMENT)
            .add("use_modifications", SerializableDataTypes.BOOLEAN, true)
            .add("calculation", SerializableDataType.enumValue(Calculation.class), Calculation.SUM)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
        data -> new EnchantmentEntityConditionType(
            data.get("enchantment"),
            data.get("use_modifications"),
            data.get("calculation"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("enchantment", conditionType.enchantmentKey)
            .set("use_modifications", conditionType.useModifications)
            .set("calculation", conditionType.calculation)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final RegistryKey<Enchantment> enchantmentKey;
    private final boolean useModifications;

    private final Calculation calculation;

    private final Comparison comparison;
    private final int compareTo;

    public EnchantmentEntityConditionType(RegistryKey<Enchantment> enchantmentKey, boolean useModifications, Calculation calculation, Comparison comparison, int compareTo) {
        this.enchantmentKey = enchantmentKey;
        this.useModifications = useModifications;
        this.calculation = calculation;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(Entity entity) {

        if (entity instanceof LivingEntity livingEntity) {

            RegistryEntry<Enchantment> enchantment = entity.getRegistryManager().get(RegistryKeys.ENCHANTMENT).entryOf(enchantmentKey);
            int level = calculation.queryTotalLevel(livingEntity, enchantment, useModifications);

            return comparison.compare(level, compareTo);

        }

        else {
            return false;
        }

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.ENCHANTMENT;
    }

    public enum Calculation {

        SUM {

            @Override
            public int queryLevel(ItemStack stack, RegistryEntry<Enchantment> enchantmentEntry, boolean useModifications, int totalLevel) {
                return ModifyEnchantmentLevelPowerType.getEnchantments(stack, stack.getEnchantments(), useModifications).getLevel(enchantmentEntry);
            }

        },

        MAX {

            @Override
            public int queryLevel(ItemStack stack, RegistryEntry<Enchantment> enchantmentEntry, boolean useModifications, int totalLevel) {

                int potentialLevel = ModifyEnchantmentLevelPowerType.getEnchantments(stack, stack.getEnchantments(), useModifications).getLevel(enchantmentEntry);

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

}
