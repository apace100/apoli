package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class EntityGlowPower extends Power {

    private final Predicate<LivingEntity> entityCondition;
    private final Predicate<Pair<LivingEntity, LivingEntity>> bientityCondition;

    public EntityGlowPower(PowerType<?> type, LivingEntity entity, Predicate<LivingEntity> entityCondition, Predicate<Pair<LivingEntity, LivingEntity>> bientityCondition) {
        super(type, entity);
        this.entityCondition = entityCondition;
        this.bientityCondition = bientityCondition;
    }

    public boolean doesApply(Entity e) {
        return e instanceof LivingEntity && (entityCondition == null || entityCondition.test((LivingEntity)e)) && (bientityCondition == null || bientityCondition.test(new Pair<>(entity, (LivingEntity)e)));
    }
}
