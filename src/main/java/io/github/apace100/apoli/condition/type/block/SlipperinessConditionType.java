package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;

public class SlipperinessConditionType {

    public static boolean condition(BlockState blockState, Comparison comparison, float compareTo) {
        return comparison.compare(blockState.getBlock().getSlipperiness(), compareTo);
    }

    public static ConditionTypeFactory<CachedBlockPosition> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("slipperiness"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.FLOAT),
            (data, cachedBlock) -> condition(cachedBlock.getBlockState(),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
