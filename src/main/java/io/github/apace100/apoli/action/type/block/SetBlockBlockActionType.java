package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public class SetBlockBlockActionType extends BlockActionType {

    public static final DataObjectFactory<SetBlockBlockActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("block", SerializableDataTypes.BLOCK_STATE),
        data -> new SetBlockBlockActionType(
            data.get("block")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("block", actionType.blockState)
    );

    private final BlockState blockState;

    public SetBlockBlockActionType(BlockState blockState) {
        this.blockState = blockState;
    }

    @Override
	protected void execute(World world, BlockPos pos, Optional<Direction> direction) {
        world.setBlockState(pos, blockState);
    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BlockActionTypes.SET_BLOCK;
    }

}
