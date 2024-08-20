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

public class AddBlockActionType {

    public static void action(World world, BlockPos pos, Direction direction, BlockState blockState) {

        if (direction != null) {
            world.setBlockState(pos.offset(direction), blockState);
        }

    }

    public static ActionTypeFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("add_block"),
            new SerializableData()
                .add("block", SerializableDataTypes.BLOCK_STATE),
            (data, block) -> action(block.getLeft(), block.getMiddle(), block.getRight(),
                data.get("block")
            )
        );
    }

}
