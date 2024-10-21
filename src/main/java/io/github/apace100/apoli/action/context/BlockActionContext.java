package io.github.apace100.apoli.action.context;

import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.util.context.TypeActionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public record BlockActionContext(World world, BlockPos pos, Optional<Direction> direction) implements TypeActionContext<BlockConditionContext> {

	@Override
	public BlockConditionContext conditionContext() {
		return new BlockConditionContext(world(), pos());
	}

}
