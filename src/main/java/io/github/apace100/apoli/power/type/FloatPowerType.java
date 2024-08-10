package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.power.Power;
import net.minecraft.entity.LivingEntity;

public class FloatPowerType extends PowerType {

    public final float value;

    public FloatPowerType(Power power, LivingEntity entity, float value) {
        super(power, entity);
        this.value = value;
    }

}
