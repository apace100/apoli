package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MovementBlockingBlockConditionType extends BlockConditionType {

    @Override
    public boolean test(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.blocksMovement()
            && !blockState.getCollisionShape(world, pos).isEmpty();
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BlockConditionTypes.MOVEMENT_BLOCKING;
    }

}
