package io.github.apace100.apoli.power.factory.condition.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import io.github.apace100.apoli.util.Comparison;

public class PoweredCondition {
    public static boolean condition(SerializableData.Instance data, CachedBlockPosition block) {
        return ((Comparison)data.get("comparison")).compare(block.getBlockEntity().getWorld().getReceivedRedstonePower(block.getBlockPos()), data.getFloat("compare_to"));
    }

    public static ConditionFactory<CachedBlockPosition> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("powered"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.FLOAT),
            PoweredCondition::condition
        );
    }
}
