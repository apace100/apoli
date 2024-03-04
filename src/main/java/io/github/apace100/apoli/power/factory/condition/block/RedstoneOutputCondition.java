package io.github.apace100.apoli.power.factory.condition.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.util.math.Direction;

public class RedstoneOutputCondition {
    public static boolean condition(SerializableData.Instance data, CachedBlockPosition block) {
        boolean passed = false;
        for(Direction direction : Direction.values()) {
            if(((Comparison)data.get("comparison")).compare(block.getBlockEntity().getWorld().getEmittedRedstonePower(block.getBlockPos(), direction), data.getFloat("compare_to"))){
                passed = true;
            }
        }
        return passed;
    }

    public static ConditionFactory<CachedBlockPosition> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("redstone_output"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.FLOAT),
            RedstoneOutputCondition::condition
        );
    }
}
