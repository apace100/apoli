package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ArmorValueItemConditionType extends ItemConditionType {

    public static final DataObjectFactory<ArmorValueItemConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
        data -> new ArmorValueItemConditionType(
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final Comparison comparison;
    private final int compareTo;

    public ArmorValueItemConditionType(Comparison comparison, int compareTo) {
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(World world, ItemStack stack) {
        return stack.getItem() instanceof ArmorItem armorItem
            && comparison.compare(armorItem.getProtection(), compareTo);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return ItemConditionTypes.ARMOR_VALUE;
    }

}
