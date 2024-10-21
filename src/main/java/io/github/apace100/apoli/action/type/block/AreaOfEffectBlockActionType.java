package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Optional;

public class AreaOfEffectBlockActionType extends BlockActionType {

    public static final TypedDataObjectFactory<AreaOfEffectBlockActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("block_action", BlockAction.DATA_TYPE)
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
            .add("radius", SerializableDataTypes.POSITIVE_INT, 16),
        data -> new AreaOfEffectBlockActionType(
            data.get("block_action"),
            data.get("block_condition"),
            data.get("shape"),
            data.get("radius")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("block_action", actionType.blockAction)
            .set("block_condition", actionType.blockCondition)
            .set("shape", actionType.shape)
            .set("radius", actionType.radius)
    );

    private final BlockAction blockAction;
    private final Optional<BlockCondition> blockCondition;

    private final Shape shape;
    private final int radius;

    public AreaOfEffectBlockActionType(BlockAction blockAction, Optional<BlockCondition> blockCondition, Shape shape, int radius) {
        this.blockAction = blockAction;
        this.blockCondition = blockCondition;
        this.shape = shape;
        this.radius = radius;
    }

    @Override
	protected void execute(World world, BlockPos pos, Optional<Direction> direction) {

        Collection<BlockPos> affectedPositions = Shape.getPositions(pos, shape, radius);

        for (BlockPos affectedPosition : affectedPositions) {

            if (blockCondition.map(condition -> condition.test(world, affectedPosition)).orElse(true)) {
                blockAction.execute(world, affectedPosition, direction);
            }

        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BlockActionTypes.AREA_OF_EFFECT;
    }

}
