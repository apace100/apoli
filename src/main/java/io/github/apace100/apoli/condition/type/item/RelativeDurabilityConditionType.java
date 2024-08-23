package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class RelativeDurabilityConditionType {

    public static boolean condition(ItemStack stack, Comparison comparison, float compareTo) {

        float durability = stack.isDamageable()
            ? (float) (stack.getMaxDamage() - stack.getDamage()) / (float) stack.getMaxDamage()
            : 1.0F;

        return comparison.compare(durability, compareTo);

    }

    public static ConditionTypeFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("relative_durability"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.FLOAT),
            (data, worldAndStack) -> condition(worldAndStack.getRight(),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
