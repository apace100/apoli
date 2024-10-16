package io.github.apace100.apoli.action.type.block.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.action.type.meta.NothingMetaActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public class NothingBlockActionType extends BlockActionType implements NothingMetaActionType {

	@Override
	protected void execute(World world, BlockPos pos, Optional<Direction> direction) {

	}

	@Override
	public ActionConfiguration<?> configuration() {
		return BlockActionTypes.NOTHING;
	}

}
