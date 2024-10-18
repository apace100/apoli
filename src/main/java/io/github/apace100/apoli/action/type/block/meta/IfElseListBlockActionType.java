package io.github.apace100.apoli.action.type.block.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.action.type.meta.IfElseListMetaActionType;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class IfElseListBlockActionType extends BlockActionType implements IfElseListMetaActionType<BlockActionContext, BlockConditionContext, BlockAction, BlockCondition> {

	private final List<ConditionedAction<BlockAction, BlockCondition>> conditionedActions;

	public IfElseListBlockActionType(List<ConditionedAction<BlockAction, BlockCondition>> conditionedActions) {
		this.conditionedActions = conditionedActions;
	}

	@Override
	protected void execute(World world, BlockPos pos, Optional<Direction> direction) {
		executeActions(new BlockActionContext(world, pos, direction));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return BlockActionTypes.IF_ELSE_LIST;
	}

	@Override
	public List<ConditionedAction<BlockAction, BlockCondition>> conditionedActions() {
		return conditionedActions;
	}

}
