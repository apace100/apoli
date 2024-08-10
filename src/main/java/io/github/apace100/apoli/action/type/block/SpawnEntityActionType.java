package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Optional;
import java.util.function.Consumer;

public class SpawnEntityActionType {

    public static void action(World world, BlockPos pos, EntityType<?> entityType, Consumer<Entity> entityAction, NbtCompound entityNbt) {

        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        Entity entityToSpawn = MiscUtil.getEntityWithPassengers(
            serverWorld,
            entityType,
            entityNbt,
            pos.toCenterPos(),
            Optional.empty(),
            Optional.empty()
        ).orElse(null);

        if (entityToSpawn == null) {
            return;
        }

        serverWorld.spawnNewEntityAndPassengers(entityToSpawn);
        entityAction.accept(entityToSpawn);

    }

    public static ActionTypeFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("spawn_entity"),
            new SerializableData()
                .add("entity_type", SerializableDataTypes.ENTITY_TYPE)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("tag", SerializableDataTypes.NBT, new NbtCompound()),
            (data, block) -> action(block.getLeft(), block.getMiddle(),
                data.get("entity_type"),
                data.getOrElse("entity_action", e -> {}),
                data.get("tag")
            )
        );
    }

}
