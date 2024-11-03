package io.github.apace100.apoli.action;

import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.action.type.block.meta.AndBlockActionType;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public final class BlockAction extends AbstractAction<BlockActionContext, BlockActionType> {

	public static final SerializableDataType<BlockAction> DATA_TYPE = SerializableDataType.lazy(() -> ApoliDataTypes.actions("type", BlockActionTypes.DATA_TYPE, AndBlockActionType::new, BlockAction::new));

	public BlockAction(BlockActionType actionType) {
		super(actionType);
	}

	public void execute(World world, BlockPos pos, Optional<Direction> direction) {
		accept(new BlockActionContext(world, pos, direction));
	}

}
