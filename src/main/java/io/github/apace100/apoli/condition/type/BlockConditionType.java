package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.context.BlockConditionContext;

public abstract class BlockConditionType extends ConditionType<BlockConditionContext, BlockCondition> {

	@Override
	public BlockCondition createCondition(boolean inverted) {
		return new BlockCondition(this, inverted);
	}

	@Override
	public boolean shouldTest(BlockConditionContext context) {
		return context.blockState() != null;
	}

}
