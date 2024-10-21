package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RelativeDurabilityItemConditionType extends ItemConditionType {

    public static final TypedDataObjectFactory<RelativeDurabilityItemConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataType.boundNumber(SerializableDataTypes.FLOAT, 0F, 1F)),
        data -> new RelativeDurabilityItemConditionType(
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final Comparison comparison;
    private final float compareTo;

    public RelativeDurabilityItemConditionType(Comparison comparison, float compareTo) {
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(World world, ItemStack stack) {

        if (stack.isDamageable()) {
            return comparison.compare(Math.abs((float) (stack.getMaxDamage() - stack.getDamage())) / (float) stack.getMaxDamage(), compareTo);
        }

        else {
            return false;
        }

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return ItemConditionTypes.RELATIVE_DURABILITY;
    }

}
