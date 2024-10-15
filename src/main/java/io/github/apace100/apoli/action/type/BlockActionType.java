package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.BlockActionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public abstract class BlockActionType extends AbstractActionType<BlockActionContext, BlockAction> {

	@Override
	public final void accept(BlockActionContext context) {
		execute(context.world(), context.pos(), context.direction());
	}

	public abstract void execute(World world, BlockPos pos, Optional<Direction> direction);

}
