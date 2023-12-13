package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;

public class ParticlePower extends Power {

    private final Predicate<Pair<Entity, Entity>> biEntityCondition;
    private final ParticleEffect particleEffect;

    private final Vec3d spread;

    private final int frequency;
    private final int count;

    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;

    private final float speed;

    private final boolean visibleInFirstPerson;
    private final boolean visibleWhileInvisible;
    private final boolean force;

    public ParticlePower(PowerType<?> powerType, LivingEntity livingEntity, ParticleEffect particleEffect, Predicate<Pair<Entity, Entity>> biEntityCondition, int count, float speed, boolean force, Vec3d spread, double offsetX, double offsetY, double offsetZ, int frequency, boolean visibleInFirstPerson, boolean visibleWhileInvisible) {
        super(powerType, livingEntity);
        this.particleEffect = particleEffect;
        this.biEntityCondition = biEntityCondition;
        this.count = Math.max(0, count);
        this.speed = speed;
        this.force = force;
        this.spread = spread;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.frequency = frequency;
        this.visibleInFirstPerson = visibleInFirstPerson;
        this.visibleWhileInvisible = visibleWhileInvisible;
    }

    public boolean doesApply(PlayerEntity viewer, boolean inFirstPerson) {
        return (!entity.isInvisibleTo(viewer) || this.isVisibleWhileInvisible())
            && (entity != viewer || (!inFirstPerson || this.isVisibleInFirstPerson()))
            && (viewer.getBlockPos().isWithinDistance(entity.getPos(), this.shouldForce() ? 512 : 32))
            && (entity.age % this.getFrequency() == 0)
            && (biEntityCondition == null || biEntityCondition.test(new Pair<>(entity, viewer)));
    }

    public ParticleEffect getParticle() {
        return particleEffect;
    }

    public Vec3d getSpread() {
        return spread;
    }

    public int getFrequency() {
        return frequency;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public double getOffsetZ() {
        return offsetZ;
    }

    public int getCount() {
        return count;
    }

    public float getSpeed() {
        return speed;
    }

    public boolean shouldForce() {
        return force;
    }

    public boolean isVisibleInFirstPerson() {
        return visibleInFirstPerson;
    }

    public boolean isVisibleWhileInvisible() {
        return visibleWhileInvisible;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("particle"),
            new SerializableData()
                .add("particle", SerializableDataTypes.PARTICLE_EFFECT_OR_TYPE)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("count", SerializableDataTypes.INT, 1)
                .add("speed", SerializableDataTypes.FLOAT, 0.0F)
                .add("force", SerializableDataTypes.BOOLEAN, false)
                .add("spread", SerializableDataTypes.VECTOR, new Vec3d(0.5, 0.5, 0.5))
                .add("offset_x", SerializableDataTypes.DOUBLE, 0.0D)
                .add("offset_y", SerializableDataTypes.DOUBLE, 0.5D)
                .add("offset_z", SerializableDataTypes.DOUBLE, 0.0D)
                .add("frequency", SerializableDataTypes.POSITIVE_INT)
                .add("visible_in_first_person", SerializableDataTypes.BOOLEAN, false)
                .add("visible_while_invisible", SerializableDataTypes.BOOLEAN, false),
            data -> (powerType, livingEntity) -> new ParticlePower(
                powerType,
                livingEntity,
                data.get("particle"),
                data.get("bientity_condition"),
                data.get("count"),
                data.get("speed"),
                data.get("force"),
                data.get("spread"),
                data.get("offset_x"),
                data.get("offset_y"),
                data.get("offset_z"),
                data.get("frequency"),
                data.get("visible_in_first_person"),
                data.get("visible_while_invisible")
            )
        ).allowCondition();
    }

}