package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Consumer;

public class BlockActionAtActionType {

    public static void action(Entity entity, Consumer<Triple<World, BlockPos, Direction>> blockAction) {
        blockAction.accept(Triple.of(entity.getWorld(), entity.getBlockPos(), Direction.UP));
    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("block_action_at"),
            new SerializableData()
                .add("block_action", ApoliDataTypes.BLOCK_ACTION),
            (data, entity) -> action(entity,
                data.get("block_action")
            )
        );
    }

}
