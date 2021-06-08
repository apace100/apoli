package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ExhaustOverTimePower extends Power {

    private final int exhaustInterval;
    private final float exhaustion;

    public ExhaustOverTimePower(PowerType<?> type, LivingEntity entity, int exhaustInterval, float exhaustion) {
        super(type, entity);
        this.exhaustInterval = exhaustInterval;
        this.exhaustion = exhaustion;
        this.setTicking();
    }

    public void tick() {
        if(entity instanceof PlayerEntity && entity.age % exhaustInterval == 0) {
            ((PlayerEntity)entity).addExhaustion(exhaustion);
        }
    }
}
