package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Consumer;

public class OffsetActionType {

    public static void action(World world, BlockPos pos, Direction direction, Consumer<Triple<World, BlockPos, Direction>> action, Vec3i offset) {
        action.accept(Triple.of(world, pos.add(offset), direction));
    }

    public static ActionTypeFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("offset"),
            new SerializableData()
                .add("action", ApoliDataTypes.BLOCK_ACTION)
                .add("x", SerializableDataTypes.INT, 0)
                .add("y", SerializableDataTypes.INT, 0)
                .add("z", SerializableDataTypes.INT, 0),
            (data, block) -> action(block.getLeft(), block.getMiddle(), block.getRight(),
                data.get("action"),
                new Vec3i(data.get("x"), data.get("y"), data.get("z"))
            )
        );
    }

}
