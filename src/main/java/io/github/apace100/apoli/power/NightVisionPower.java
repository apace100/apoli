package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Function;

public class NightVisionPower extends Power {

    private final Function<LivingEntity, Float> strengthFunction;

    public NightVisionPower(PowerType<?> type, LivingEntity entity) {
        this(type, entity, 1.0F);
    }

    public NightVisionPower(PowerType<?> type, LivingEntity entity, float strength) {
        this(type, entity, pe -> strength);
    }

    public NightVisionPower(PowerType<?> type, LivingEntity entity, Function<LivingEntity, Float> strengthFunction) {
        super(type, entity);
        this.strengthFunction = strengthFunction;
    }

    public float getStrength() {
        return strengthFunction.apply(this.entity);
    }
}
