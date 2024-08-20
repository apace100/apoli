package io.github.apace100.apoli.condition.type.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

public class AttachableConditionType {

    public static boolean condition(CachedBlockPosition cachedBlock) {

        WorldView worldView = cachedBlock.getWorld();
        BlockPos originPos = cachedBlock.getBlockPos();

        for (Direction direction : Direction.values()) {

            BlockPos offsetPos = originPos.offset(direction);
            BlockState adjacentState = worldView.getBlockState(offsetPos);

            if (adjacentState.isSideSolidFullSquare(worldView, originPos, direction.getOpposite())) {
                return true;
            }

        }

        return false;

    }

}
