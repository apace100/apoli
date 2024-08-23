package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;

public class HeightConditionType {

    public static boolean condition(CachedBlockPosition cachedBlock, Comparison comparison, int compareTo) {
        return comparison.compare(cachedBlock.getBlockPos().getY(), compareTo);
    }

    public static ConditionTypeFactory<CachedBlockPosition> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("height"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            (data, cachedBlock) -> condition(cachedBlock,
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
