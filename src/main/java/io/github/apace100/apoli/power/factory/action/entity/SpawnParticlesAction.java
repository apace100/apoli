package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class SpawnParticlesAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if(entity.getWorld().isClient) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld) entity.getWorld();
        int count = data.get("count");
        if(count <= 0)
            return;
        float speed = data.get("speed");
        Vec3d spread = data.get("spread");
        float deltaX = (float) (entity.getWidth() * spread.x);
        float deltaY = (float) (entity.getHeight() * spread.y);
        float deltaZ = (float) (entity.getWidth() * spread.z);
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
                .add("spread", SerializableDataTypes.VECTOR, new Vec3d(0.5, 0.25, 0.5))
                .add("offset_y", SerializableDataTypes.FLOAT, 0.5F),
            SpawnParticlesAction::action
        );
    }
}
