package io.github.apace100.apoli.action.type.block.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.action.type.meta.AndMetaActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class AndBlockActionType extends BlockActionType implements AndMetaActionType<BlockActionContext, BlockAction> {

	private final List<BlockAction> actions;

	public AndBlockActionType(List<BlockAction> actions) {
		this.actions = actions;
	}

	@Override
	public void execute(World world, BlockPos pos, Optional<Direction> direction) {
		executeActions(new BlockActionContext(world, pos, direction));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return BlockActionTypes.AND;
	}

	@Override
	public List<BlockAction> actions() {
		return actions;
	}

}
