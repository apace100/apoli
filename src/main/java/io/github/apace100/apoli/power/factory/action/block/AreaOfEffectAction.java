package io.github.apace100.apoli.power.factory.action.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
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

public class AreaOfEffectAction {

    public static void action(SerializableData.Instance data, Triple<World, BlockPos, Direction> block) {

        World world = block.getLeft();
        BlockPos blockPos = block.getMiddle();
        Direction direction = block.getRight();

        int radius = data.get("radius");

        Shape shape = data.get("shape");
        Predicate<CachedBlockPosition> blockCondition = data.get("block_condition");
        Consumer<Triple<World, BlockPos, Direction>> blockAction = data.get("block_action");

        for (BlockPos collectedBlockPos : Shape.getPositions(blockPos, shape, radius)) {
            if (!(blockCondition == null || blockCondition.test(new CachedBlockPosition(world, collectedBlockPos, true)))) continue;
            if (blockAction != null) blockAction.accept(Triple.of(world, collectedBlockPos, direction));
        }

    }

    public static ActionFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("area_of_effect"),
            new SerializableData()
                .add("block_action", ApoliDataTypes.BLOCK_ACTION)
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("radius", SerializableDataTypes.INT, 16)
                .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE),
            AreaOfEffectAction::action
        );
    }

}
