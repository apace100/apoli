package io.github.apace100.apoli.condition.type.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;

public class MovementBlockingConditionType {

    public static boolean condition(CachedBlockPosition cachedBlock) {
        BlockState state = cachedBlock.getBlockState();
        return state.blocksMovement()
            && !state.getCollisionShape(cachedBlock.getWorld(), cachedBlock.getBlockPos()).isEmpty();
    }

}
