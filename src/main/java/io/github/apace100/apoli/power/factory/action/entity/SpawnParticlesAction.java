package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

public class SpawnParticlesAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if(entity.world.isClient) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld) entity.world;
        int count = data.get("count");
        if(count <= 0)
            return;
        float speed = data.get("speed");
        float deltaX = entity.getWidth() * data.getFloat("spread_x");
        float deltaY = entity.getHeight() * data.getFloat("spread_y");
        float deltaZ = entity.getWidth() * data.getFloat("spread_z");
        float offsetY = entity.getHeight() * data.getFloat("offset_y");
        serverWorld.spawnParticles(data.get("particle"), entity.getX(), entity.getY() + offsetY, entity.getZ(), count, deltaX, deltaY, deltaZ, speed);
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("spawn_particles"),
            new SerializableData()
                .add("particle", SerializableDataTypes.PARTICLE_EFFECT_OR_TYPE)
                .add("count", SerializableDataTypes.INT)
                .add("speed", SerializableDataTypes.FLOAT, 0.0F)
                .add("force", SerializableDataTypes.BOOLEAN, false)
                .add("spread_x", SerializableDataTypes.FLOAT, 0.5F)
                .add("spread_y", SerializableDataTypes.FLOAT, 0.25F)
                .add("spread_z", SerializableDataTypes.FLOAT, 0.5F)
                .add("offset_y", SerializableDataTypes.FLOAT, 0.5F),
            SpawnParticlesAction::action
        );
    }
}
