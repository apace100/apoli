package io.github.apace100.apoli.action.type.block.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.action.type.meta.DelayMetaActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public class DelayBlockActionType extends BlockActionType implements DelayMetaActionType<BlockActionContext, BlockAction> {

	private final BlockAction action;
	private final int ticks;

	public DelayBlockActionType(BlockAction action, int ticks) {
		this.action = action;
		this.ticks = ticks;
	}

	@Override
	public void execute(World world, BlockPos pos, Optional<Direction> direction) {
		executeAction(new BlockActionContext(world, pos, direction));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return BlockActionTypes.DELAY;
	}

	@Override
	public BlockAction action() {
		return action;
	}

	@Override
	public int ticks() {
		return ticks;
	}

}
