package io.github.apace100.apoli.action.type.block.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Optional;

public class OffsetBlockActionType extends BlockActionType {

    public static final DataObjectFactory<OffsetBlockActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("action", BlockAction.DATA_TYPE)
            .add("x", SerializableDataTypes.INT, 0)
            .add("y", SerializableDataTypes.INT, 0)
            .add("z", SerializableDataTypes.INT, 0),
        data -> new OffsetBlockActionType(
            data.get("action"),
            new Vec3i(
                data.get("x"),
                data.get("y"),
                data.get("z")
            )
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("action", actionType.blockAction)
            .set("x", actionType.offset.getX())
            .set("y", actionType.offset.getY())
            .set("z", actionType.offset.getZ())
    );

    private final BlockAction blockAction;
    private final Vec3i offset;

    public OffsetBlockActionType(BlockAction blockAction, Vec3i offset) {
        this.blockAction = blockAction;
        this.offset = offset;
    }

    @Override
    public void execute(World world, BlockPos pos, Optional<Direction> direction) {
        blockAction.execute(world, pos.add(offset), direction);
    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BlockActionTypes.OFFSET;
    }

}
