package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class AreaOfEffectActionType {

    public static void action(World world, BlockPos pos, Direction direction, Consumer<Triple<World, BlockPos, Direction>> blockAction, Predicate<CachedBlockPosition> blockCondition, Shape shape, int radius) {

        for (BlockPos collectedPos : Shape.getPositions(pos, shape, radius)) {

            if (blockCondition.test(new CachedBlockPosition(world, collectedPos, true))) {
                blockAction.accept(Triple.of(world, collectedPos, direction));
            }

        }

    }

    public static ActionTypeFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("area_of_effect"),
            new SerializableData()
                .add("block_action", ApoliDataTypes.BLOCK_ACTION)
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
                .add("radius", SerializableDataTypes.INT, 16),
            (data, block) -> action(block.getLeft(), block.getMiddle(), block.getRight(),
                data.get("block_action"),
                data.getOrElse("block_condition", cachedBlock -> true),
                data.get("shape"),
                data.get("radius")
            )
        );
    }

}
