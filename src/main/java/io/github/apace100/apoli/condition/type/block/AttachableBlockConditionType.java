package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class AttachableBlockConditionType extends BlockConditionType {

    @Override
    public boolean test(World world, BlockPos pos) {

        for (Direction direction : Direction.values()) {

            BlockPos offsetPos = pos.offset(direction);
            BlockState adjacentBlockState = world.getBlockState(offsetPos);

            if (adjacentBlockState.isSideSolidFullSquare(world, pos, direction.getOpposite())) {
                return true;
            }

        }

        return false;

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BlockConditionTypes.ATTACHABLE;
    }

}
