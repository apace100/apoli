package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.Block;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InTagBlockConditionType extends BlockConditionType {

    public static final TypedDataObjectFactory<InTagBlockConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("tag", SerializableDataTypes.BLOCK_TAG),
        data -> new InTagBlockConditionType(
            data.get("tag")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("tag", conditionType.tag)
    );

    private final TagKey<Block> tag;

    public InTagBlockConditionType(TagKey<Block> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(World world, BlockPos pos) {
        return world.getBlockState(pos).isIn(tag);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BlockConditionTypes.IN_TAG;
    }

}
