package io.github.apace100.apoli.action.type.entity;

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
import net.minecraft.util.Pair;

import java.util.function.Consumer;

public class SpawnEntityActionType {

    public static void action(Entity entity, EntityType<?> entityType, Consumer<Entity> entityAction, Consumer<Pair<Entity, Entity>> biEntityAction, NbtCompound entityNbt) {

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        Entity entityToSpawn = MiscUtil.getEntityWithPassengers(
            serverWorld,
            entityType,
            entityNbt,
            entity.getPos(),
            entity.getYaw(),
            entity.getPitch()
        ).orElse(null);

        if (entityToSpawn == null) {
            return;
        }

        serverWorld.spawnNewEntityAndPassengers(entityToSpawn);

        entityAction.accept(entityToSpawn);
        biEntityAction.accept(new Pair<>(entity, entityToSpawn));

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("spawn_entity"),
            new SerializableData()
                .add("entity_type", SerializableDataTypes.ENTITY_TYPE)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("tag", SerializableDataTypes.NBT, null),
            (data, entity) -> action(entity,
                data.get("entity_type"),
                data.getOrElse("entity_action", e -> {}),
                data.getOrElse("bientity_action", at -> {}),
                data.get("tag")
            )
        );
    }

}
