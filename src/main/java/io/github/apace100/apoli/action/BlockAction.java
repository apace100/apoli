package io.github.apace100.apoli.action;

import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.action.type.block.meta.SequenceBlockActionType;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class BlockAction extends Action<BlockActionContext, BlockActionType> {

	public static final SerializableDataType<BlockAction> DATA_TYPE = SerializableDataType.lazy(() -> ApoliDataTypes.actions("type", BlockActionTypes.DATA_TYPE, SequenceBlockActionType::new, BlockAction::new));

	public BlockAction(BlockActionType actionType) {
		super(actionType);
	}

	public void execute(World world, BlockPos pos) {
		execute(world, pos, Optional.empty());
	}

	public void execute(World world, BlockPos pos, @Nullable Direction direction) {
		execute(world, pos, Optional.ofNullable(direction));
	}

	public void execute(World world, BlockPos pos, Optional<Direction> direction) {

		if (world instanceof ServerWorld serverWorld) {
			accept(new BlockActionContext(serverWorld, pos, direction));
		}

	}

}
