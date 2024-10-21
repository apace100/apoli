package io.github.apace100.apoli.condition.context;

import io.github.apace100.apoli.util.context.TypeConditionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record BlockConditionContext(World world, BlockPos pos) implements TypeConditionContext {

}
