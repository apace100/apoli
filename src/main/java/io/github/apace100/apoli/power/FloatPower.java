package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;

public class FloatPower extends Power {

    public final float value;

    public FloatPower(PowerType<?> type, LivingEntity entity, float value) {
        super(type, entity);
        this.value = value;
    }
}
