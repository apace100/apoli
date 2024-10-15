package io.github.apace100.apoli.condition.context;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record BlockConditionContext(World world, BlockPos pos) {

}
