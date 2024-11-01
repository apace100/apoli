package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public abstract class BlockConditionType extends AbstractConditionType<BlockConditionContext, BlockCondition> {

	@Override
	public BlockCondition createCondition(boolean inverted) {
		return new BlockCondition(this, inverted);
	}

	@Override
	public boolean test(BlockConditionContext context) {
		return context.blockState() != null
			&& test(context.world(), context.pos(), context.blockState(), context.blockEntity());
	}

	public abstract boolean test(World world, BlockPos pos, BlockState blockState, Optional<BlockEntity> blockEntity);

}
