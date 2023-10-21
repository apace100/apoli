package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class ArmorValueCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        Comparison comparison = data.get("comparison");
        int compareTo = data.get("compare_to");

        return worldAndStack.getRight().getItem() instanceof ArmorItem armorItem
            && comparison.compare(armorItem.getProtection(), compareTo);

    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("armor_value"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            ArmorValueCondition::condition
        );
    }

}
