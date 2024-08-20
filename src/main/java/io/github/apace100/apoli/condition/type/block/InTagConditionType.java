package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.Block;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.registry.tag.TagKey;

public class InTagConditionType {

    public static boolean condition(CachedBlockPosition cachedBlock, TagKey<Block> blockTag) {
        return cachedBlock.getBlockState().isIn(blockTag);
    }

    public static ConditionTypeFactory<CachedBlockPosition> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("in_tag"),
            new SerializableData()
                .add("tag", SerializableDataTypes.BLOCK_TAG),
            (data, cachedBlock) -> condition(cachedBlock,
                data.get("tag")
            )
        );
    }

}
