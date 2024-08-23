package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;

public class BlockConditionType {

    public static boolean condition(CachedBlockPosition cachedBlock, Block block) {
        return cachedBlock.getBlockState().isOf(block);
    }

    public static ConditionTypeFactory<CachedBlockPosition> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("block"),
            new SerializableData()
                .add("block", SerializableDataTypes.BLOCK),
            (data, cachedBlock) -> condition(cachedBlock,
                data.get("block")
            )
        );
    }

}
