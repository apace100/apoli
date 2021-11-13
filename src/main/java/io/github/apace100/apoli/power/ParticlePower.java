package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;

public class ParticlePower extends Power {

    private final ParticleEffect particleEffect;
    private final int frequency;
    private final boolean visibleInFirstPerson;

    public ParticlePower(PowerType<?> type, LivingEntity entity, ParticleEffect particle, int frequency, boolean visibleInFirstPerson) {
        super(type, entity);
        this.particleEffect = particle;
        this.frequency = frequency;
        this.visibleInFirstPerson = visibleInFirstPerson;
    }

    public ParticleEffect getParticle() {
        return particleEffect;
    }

    public int getFrequency() {
        return frequency;
    }

    public boolean isVisibleInFirstPerson() {
        return visibleInFirstPerson;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("particle"),
            new SerializableData()
                .add("particle", SerializableDataTypes.PARTICLE_EFFECT_OR_TYPE)
                .add("frequency", SerializableDataTypes.INT)
                .add("visible_in_first_person", SerializableDataTypes.BOOLEAN, false),
            data ->
                (type, player) ->
                    new ParticlePower(type, player, data.get("particle"), data.getInt("frequency"), data.getBoolean("visible_in_first_person")))
            .allowCondition();
    }
}