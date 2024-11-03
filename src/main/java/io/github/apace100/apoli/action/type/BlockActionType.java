package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.BlockActionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public abstract class BlockActionType extends AbstractActionType<BlockActionContext, BlockAction> {

	@Override
	public void accept(BlockActionContext context) {
		execute(context.world(), context.pos(), context.direction());
	}

	@Override
	public BlockAction createAction() {
		return new BlockAction(this);
	}

	protected abstract void execute(World world, BlockPos pos, Optional<Direction> direction);

}
