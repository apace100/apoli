package io.github.apace100.apoli.power.factory.action.entity;

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

import java.util.Optional;
import java.util.function.Consumer;

public class SpawnEntityAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        if (entity.getWorld().isClient) return;

        ServerWorld serverWorld = (ServerWorld) entity.getWorld();
        EntityType<?> entityType = data.get("entity_type");
        NbtCompound entityNbt = data.get("tag");

        Optional<Entity> opt$entityToSpawn = MiscUtil.getEntityWithPassengers(
            serverWorld,
            entityType,
            entityNbt,
            entity.getPos(),
            entity.getYaw(),
            entity.getPitch()
        );

        if (opt$entityToSpawn.isEmpty()) return;
        Entity entityToSpawn = opt$entityToSpawn.get();

        serverWorld.spawnNewEntityAndPassengers(entityToSpawn);
        data.<Consumer<Entity>>ifPresent("entity_action", entityAction -> entityAction.accept(entityToSpawn));

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("spawn_entity"),
            new SerializableData()
                .add("entity_type", SerializableDataTypes.ENTITY_TYPE)
                .add("tag", SerializableDataTypes.NBT, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
            SpawnEntityAction::action
        );
    }

}
