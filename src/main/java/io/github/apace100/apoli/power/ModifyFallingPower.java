package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;

public class ModifyFallingPower extends Power {

    public final double velocity;
    public final boolean takeFallDamage;

    public ModifyFallingPower(PowerType<?> type, LivingEntity entity, double velocity, boolean takeFallDamage) {
        super(type, entity);
        this.velocity = velocity;
        this.takeFallDamage = takeFallDamage;
    }
}
