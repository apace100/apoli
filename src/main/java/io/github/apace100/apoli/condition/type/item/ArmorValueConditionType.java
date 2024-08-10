package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class ArmorValueConditionType {

    public static boolean condition(ItemStack stack, Comparison comparison, int compareTo) {

        int protection = stack.getItem() instanceof ArmorItem armorItem
            ? armorItem.getProtection()
            : 0;

        return comparison.compare(protection, compareTo);

    }

    public static ConditionTypeFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("armor_value"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            (data, worldAndStack) -> condition(worldAndStack.getRight(),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
