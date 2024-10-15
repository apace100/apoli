package io.github.apace100.apoli.action.type.block.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.action.type.meta.ChanceMetaActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public class ChanceBlockActionType extends BlockActionType implements ChanceMetaActionType<BlockActionContext, BlockAction> {

	private final BlockAction successAction;
	private final Optional<BlockAction> failAction;

	private final float chance;

	public ChanceBlockActionType(BlockAction successAction, Optional<BlockAction> failAction, float chance) {
		this.successAction = successAction;
		this.failAction = failAction;
		this.chance = chance;
	}

	@Override
	public void execute(World world, BlockPos pos, Optional<Direction> direction) {
		executeAction(new BlockActionContext(world, pos, direction));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return BlockActionTypes.CHANCE;
	}

	@Override
	public BlockAction successAction() {
		return successAction;
	}

	@Override
	public Optional<BlockAction> failAction() {
		return failAction;
	}

	@Override
	public float chance() {
		return chance;
	}

}
