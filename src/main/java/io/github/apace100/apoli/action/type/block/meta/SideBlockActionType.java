package io.github.apace100.apoli.action.type.block.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.action.type.meta.SideMetaActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public class SideBlockActionType extends BlockActionType implements SideMetaActionType<BlockActionContext, BlockAction> {

	private final BlockAction action;
	private final Side side;

	public SideBlockActionType(BlockAction action, Side side) {
		this.action = action;
		this.side = side;
	}

	@Override
	public void execute(World world, BlockPos pos, Optional<Direction> direction) {
		executeAction(new BlockActionContext(world, pos, direction));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return BlockActionTypes.SIDE;
	}

	@Override
	public BlockAction action() {
		return action;
	}

	@Override
	public Side side() {
		return side;
	}

}
