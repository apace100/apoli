package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class SpawnParticlesEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<SpawnParticlesEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("particle", SerializableDataTypes.PARTICLE_EFFECT_OR_TYPE)
            .add("offset_x", SerializableDataTypes.DOUBLE, 0.5D)
            .add("offset_y", SerializableDataTypes.DOUBLE, 0.5D)
            .add("offset_z", SerializableDataTypes.DOUBLE, 0.5D)
            .add("spread", SerializableDataTypes.VECTOR, new Vec3d(0.5D, 0.5D, 0.5D))
            .add("force", SerializableDataTypes.BOOLEAN, false)
            .add("speed", SerializableDataTypes.FLOAT, 0.0F)
            .add("count", SerializableDataTypes.INT, 1),
        data -> new SpawnParticlesEntityActionType(
            data.get("bientity_condition"),
            data.get("particle"),
            new Vec3d(
                data.get("offset_x"),
                data.get("offset_y"),
                data.get("offset_z")
            ),
            data.get("spread"),
            data.get("force"),
            data.get("speed"),
            data.get("count")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("bientity_condition", actionType.biEntityCondition)
            .set("particle", actionType.particle)
            .set("offset_x", actionType.offset.x)
            .set("offset_y", actionType.offset.y)
            .set("offset_z", actionType.offset.z)
            .set("spread", actionType.spread)
            .set("force", actionType.force)
            .set("speed", actionType.speed)
            .set("count", actionType.count)
    );

    private final Optional<BiEntityCondition> biEntityCondition;
    private final ParticleEffect particle;

    private final Vec3d offset;
    private final Vec3d spread;

    private final boolean force;
    private final float speed;
    private final int count;

    public SpawnParticlesEntityActionType(Optional<BiEntityCondition> biEntityCondition, ParticleEffect particle, Vec3d offset, Vec3d spread, boolean force, float speed, int count) {
        this.biEntityCondition = biEntityCondition;
        this.particle = particle;
        this.offset = offset;
        this.spread = spread;
        this.force = force;
        this.speed = speed;
        this.count = count;
    }

    @Override
    protected void execute(Entity entity) {

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        Vec3d delta = spread.multiply(entity.getWidth(), entity.getHeight(), entity.getWidth());
        Vec3d pos = entity.getPos().add(offset);

        for (ServerPlayerEntity player : serverWorld.getPlayers()) {

            if (biEntityCondition.map(condition -> condition.test(entity, player)).orElse(true)) {
                serverWorld.spawnParticles(player, particle, force, pos.getX(), pos.getY(), pos.getZ(), count, delta.getX(), delta.getY(), delta.getZ(), speed);
            }

        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.SPAWN_PARTICLES;
    }

}
