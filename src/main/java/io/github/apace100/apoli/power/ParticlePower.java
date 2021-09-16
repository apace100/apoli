package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
}