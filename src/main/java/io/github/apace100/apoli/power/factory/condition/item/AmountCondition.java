package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class AmountCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        Comparison comparison = data.get("comparison");
        int compareTo = data.get("compare_to");

        return comparison.compare(worldAndStack.getRight().getCount(), compareTo);

    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("amount"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            AmountCondition::condition
        );
    }

}
