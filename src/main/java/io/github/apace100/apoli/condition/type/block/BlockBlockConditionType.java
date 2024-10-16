package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBlockConditionType extends BlockConditionType {

    public static final DataObjectFactory<BlockBlockConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("block", SerializableDataTypes.BLOCK),
        data -> new BlockBlockConditionType(
            data.get("block")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("block", conditionType.block)
    );

    private final Block block;

    public BlockBlockConditionType(Block block) {
        this.block = block;
    }

    @Override
    public boolean test(World world, BlockPos pos) {
        return world.getBlockState(pos).isOf(block);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BlockConditionTypes.BLOCK;
    }

}
