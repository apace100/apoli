package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;

public class SpawnParticlesActionType {

    public static void action(Entity entity, ParticleEffect particle, Predicate<Pair<Entity, Entity>> biEntityCondition, Vec3d offset, Vec3d spread, boolean force, float speed, int count) {

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        Vec3d delta = spread.multiply(entity.getWidth(), entity.getHeight(), entity.getWidth());
        Vec3d pos = entity.getPos().add(offset);

        for (ServerPlayerEntity player : serverWorld.getPlayers()) {

            if (biEntityCondition.test(new Pair<>(entity, player))) {
                serverWorld.spawnParticles(player, particle, force, pos.getX(), pos.getY(), pos.getZ(), count, delta.getX(), delta.getY(), delta.getZ(), speed);
            }

        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(Apoli.identifier("spawn_particles"),
            new SerializableData()
                .add("particle", SerializableDataTypes.PARTICLE_EFFECT_OR_TYPE)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("offset_x", SerializableDataTypes.DOUBLE, 0.0D)
                .add("offset_y", SerializableDataTypes.DOUBLE, 0.5D)
                .add("offset_z", SerializableDataTypes.DOUBLE, 0.0D)
                .add("spread", SerializableDataTypes.VECTOR, new Vec3d(0.5, 0.5, 0.5))
                .add("force", SerializableDataTypes.BOOLEAN, false)
                .add("speed", SerializableDataTypes.FLOAT, 0.0F)
                .add("count", SerializableDataTypes.INT, 1),
            (data, entity) -> action(entity,
                data.get("particle"),
                data.getOrElse("bientity_condition", actorAndTarget -> true),
                new Vec3d(data.get("offset_x"), data.get("offset_y"), data.get("offset_z")),
                data.get("spread"),
                data.get("force"),
                data.get("speed"),
                data.get("count")
            )
        );
    }

}
