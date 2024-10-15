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

public class AddBlockBlockActionType extends BlockActionType {

    public static final DataObjectFactory<AddBlockBlockActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("block", SerializableDataTypes.BLOCK_STATE),
        data -> new AddBlockBlockActionType(
            data.get("block")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("block", actionType.blockState)
    );

    private final BlockState blockState;

    public AddBlockBlockActionType(BlockState blockState) {
        this.blockState = blockState;
    }

    @Override
    public void execute(World world, BlockPos pos, Optional<Direction> direction) {
        direction.ifPresent(dir -> world.setBlockState(pos.offset(dir), blockState));
    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BlockActionTypes.ADD_BLOCK;
    }

}
