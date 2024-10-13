package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.type.ModifyEnchantmentLevelPowerType;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import java.util.Optional;

public class EnchantmentItemConditionType extends ItemConditionType {

    public static final DataObjectFactory<EnchantmentItemConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("enchantment", SerializableDataTypes.ENCHANTMENT.optional(), Optional.empty())
            .add("use_modifications", SerializableDataTypes.BOOLEAN, true)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
            .add("compare_to", SerializableDataTypes.INT, 0),
        data -> new EnchantmentItemConditionType(
            data.get("enchantment"),
            data.get("use_modifications"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("enchantment", conditionType.enchantmentKey)
            .set("use_modifications", conditionType.useModifications)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final Optional<RegistryKey<Enchantment>> enchantmentKey;
    private final boolean useModifications;

    private final Comparison comparison;
    private final int compareTo;

    public EnchantmentItemConditionType(Optional<RegistryKey<Enchantment>> enchantmentKey, boolean useModifications, Comparison comparison, int compareTo) {
        this.enchantmentKey = enchantmentKey;
        this.useModifications = useModifications;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(World world, ItemStack stack) {

        ItemEnchantmentsComponent enchantmentsComponent = ModifyEnchantmentLevelPowerType.getEnchantments(stack, stack.getEnchantments(), useModifications);
        int levelOrEnchantments = enchantmentKey
            .map(key -> world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).entryOf(key))
            .map(enchantmentsComponent::getLevel)
            .orElseGet(enchantmentsComponent::getSize);

        return comparison.compare(levelOrEnchantments, compareTo);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return ItemConditionTypes.ENCHANTMENT;
    }

}
