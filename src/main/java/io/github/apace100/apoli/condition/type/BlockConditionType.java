package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockConditionType extends AbstractConditionType<BlockConditionContext, BlockCondition> {

	@Override
	public final boolean test(BlockConditionContext context) {
		return test(context.world(), context.pos());
	}

	@Override
	public BlockCondition createCondition(boolean inverted) {
		return new BlockCondition(this, inverted);
	}

	public abstract boolean test(World world, BlockPos pos);

}
