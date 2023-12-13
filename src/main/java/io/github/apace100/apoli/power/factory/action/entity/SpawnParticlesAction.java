package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;

public class SpawnParticlesAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        Vec3d delta = data
            .<Vec3d>get("spread")
            .multiply(entity.getWidth(), entity.getEyeHeight(entity.getPose()), entity.getWidth());
        Vec3d pos = entity
            .getPos()
            .add(data.getDouble("offset_x"), data.getDouble("offset_y"), data.getDouble("offset_z"));

        Predicate<Pair<Entity, Entity>> biEntityCondition = data.get("bientity_condition");
        ParticleEffect particleEffect = data.get("particle");

        boolean force = data.getBoolean("force");
        float speed = data.getFloat("speed");
        int count = Math.max(0, data.getInt("count"));

        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            if (biEntityCondition == null || biEntityCondition.test(new Pair<>(entity, player))) {
                serverWorld.spawnParticles(player, particleEffect, force, pos.getX(), pos.getY(), pos.getZ(), count, delta.getX(), delta.getY(), delta.getZ(), speed);
            }
        }

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("spawn_particles"),
            new SerializableData()
                .add("particle", SerializableDataTypes.PARTICLE_EFFECT_OR_TYPE)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("count", SerializableDataTypes.INT, 1)
                .add("speed", SerializableDataTypes.FLOAT, 0.0F)
                .add("force", SerializableDataTypes.BOOLEAN, false)
                .add("spread", SerializableDataTypes.VECTOR, new Vec3d(0.5, 0.5, 0.5))
                .add("offset_x", SerializableDataTypes.DOUBLE, 0.0D)
                .add("offset_y", SerializableDataTypes.DOUBLE, 0.5D)
                .add("offset_z", SerializableDataTypes.DOUBLE, 0.0D),
            SpawnParticlesAction::action
        );
    }
}
