package io.github.apace100.apoli.power.factory.condition.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

public class RedstoneOutputCondition {
    public static boolean condition(SerializableData.Instance data, CachedBlockPosition block) {
        BlockPos blockPos = block.getBlockPos();
        WorldView world = block.getWorld();

        Comparison comparison = data.get("comparison");
        int compareTo = data.getInt("compare_to");

        for (Direction direction : Direction.values()) {

            int emittedRedstonePower = world.getEmittedRedstonePower(blockPos, direction);
            
            if (comparison.compare(emittedRedstonePower, compareTo)) {
                return true;
            }

        }
        
        return false;
    }

    public static ConditionFactory<CachedBlockPosition> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("redstone_output"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            RedstoneOutputCondition::condition
        );
    }
}
