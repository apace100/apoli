package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

public class SetBlockActionType {

    public static void action(World world, BlockPos pos, BlockState blockState) {
        world.setBlockState(pos, blockState);
    }

    public static ActionTypeFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("set_block"),
            new SerializableData()
                .add("block", SerializableDataTypes.BLOCK_STATE),
            (data, block) -> action(block.getLeft(), block.getMiddle(),
                data.get("block")
            )
        );
    }

}
