package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Predicate;

public class PreventEntityRenderPower extends Power {

    private final Predicate<Entity> entityCondition;

    public PreventEntityRenderPower(PowerType<?> type, LivingEntity entity, Predicate<Entity> entityCondition) {
        super(type, entity);
        this.entityCondition = entityCondition;
    }

    public boolean doesApply(Entity e) {
        return (entityCondition == null || entityCondition.test(e));
    }
}
