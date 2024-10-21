package io.github.apace100.apoli.action.type.block.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.action.type.meta.IfElseMetaActionType;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public class IfElseBlockActionType extends BlockActionType implements IfElseMetaActionType<BlockActionContext, BlockConditionContext, BlockAction, BlockCondition> {

	private final BlockCondition condition;

	private final BlockAction ifAction;
	private final Optional<BlockAction> elseAction;

	public IfElseBlockActionType(BlockCondition condition, BlockAction ifAction, Optional<BlockAction> elseAction) {
		this.condition = condition;
		this.ifAction = ifAction;
		this.elseAction = elseAction;
	}

	@Override
	protected void execute(World world, BlockPos pos, Optional<Direction> direction) {
		executeAction(new BlockActionContext(world, pos, direction));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return BlockActionTypes.IF_ELSE;
	}

	@Override
	public BlockCondition condition() {
		return condition;
	}

	@Override
	public BlockAction ifAction() {
		return ifAction;
	}

	@Override
	public Optional<BlockAction> elseAction() {
		return elseAction;
	}

}
