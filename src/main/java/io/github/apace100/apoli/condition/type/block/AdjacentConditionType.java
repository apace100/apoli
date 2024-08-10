package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.util.math.Direction;

import java.util.function.Predicate;

public class AdjacentConditionType {

    public static boolean condition(CachedBlockPosition cachedBlock, Predicate<CachedBlockPosition> adjacentCondition, Comparison comparison, int compareTo) {

        int matchingAdjacents = 0;
        for (Direction direction : Direction.values()) {

            if (adjacentCondition.test(new CachedBlockPosition(cachedBlock.getWorld(), cachedBlock.getBlockPos().offset(direction), true))) {
                matchingAdjacents++;
            }

        }

        return comparison.compare(matchingAdjacents, compareTo);

    }

    public static ConditionTypeFactory<CachedBlockPosition> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("adjacent"),
            new SerializableData()
                .add("adjacent_condition", ApoliDataTypes.BLOCK_CONDITION)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            (data, cachedBlock) -> condition(cachedBlock,
                data.get("adjacent_condition"),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
