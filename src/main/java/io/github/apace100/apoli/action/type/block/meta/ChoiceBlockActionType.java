package io.github.apace100.apoli.action.type.block.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.action.type.meta.ChoiceMetaActionType;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public class ChoiceBlockActionType extends BlockActionType implements ChoiceMetaActionType<BlockActionContext, BlockAction> {

	private final WeightedList<BlockAction> actions;

	public ChoiceBlockActionType(WeightedList<BlockAction> actions) {
		this.actions = actions;
	}

	@Override
	protected void execute(World world, BlockPos pos, Optional<Direction> direction) {
		executeActions(new BlockActionContext(world, pos, direction));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return BlockActionTypes.CHOICE;
	}

	@Override
	public WeightedList<BlockAction> actions() {
		return actions;
	}

}
