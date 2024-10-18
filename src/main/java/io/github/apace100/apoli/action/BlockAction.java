package io.github.apace100.apoli.action;

import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public class BlockAction extends AbstractAction<BlockActionContext, BlockActionType> {

	public static final SerializableDataType<BlockAction> DATA_TYPE = SerializableDataType.lazy(() -> ApoliDataTypes.action("type", BlockActionTypes.DATA_TYPE, BlockAction::new));

	public BlockAction(BlockActionType actionType) {
		super(actionType);
	}

	public void execute(World world, BlockPos pos, Optional<Direction> direction) {
		accept(new BlockActionContext(world, pos, direction));
	}

}
