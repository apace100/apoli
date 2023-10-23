package io.github.apace100.apoli.power.factory.action.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
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

public class SpawnEntityAction {

    public static void action(SerializableData.Instance data, Triple<World, BlockPos, Direction> worldPosAndDirection) {

        World world = worldPosAndDirection.getLeft();
        BlockPos pos = worldPosAndDirection.getMiddle();

        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        EntityType<?> entityType = data.get("entity_type");
        NbtCompound entityNbt = data.get("tag");

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
        data.<Consumer<Entity>>ifPresent("entity_action", entityAction -> entityAction.accept(entityToSpawn));

    }

    public static ActionFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("spawn_entity"),
            new SerializableData()
                .add("entity_type", SerializableDataTypes.ENTITY_TYPE)
                .add("tag", SerializableDataTypes.NBT, new NbtCompound())
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
            SpawnEntityAction::action
        );
    }

}
