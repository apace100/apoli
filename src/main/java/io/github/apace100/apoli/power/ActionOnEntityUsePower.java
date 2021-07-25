package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnEntityUsePower extends Power {

    private final Consumer<Pair<Entity, Entity>> biEntityAction;
    private final Predicate<LivingEntity> otherCondition;

    public ActionOnEntityUsePower(PowerType<?> type, LivingEntity entity, Consumer<Pair<Entity, Entity>> biEntityAction, Predicate<LivingEntity> otherCondition) {
        super(type, entity);
        this.biEntityAction = biEntityAction;
        this.otherCondition = otherCondition;
    }

    public boolean shouldExecute(Entity other) {
        return !(other instanceof LivingEntity) || (otherCondition == null || otherCondition.test((LivingEntity) other));
    }

    public ActionResult executeAction(Entity other) {
        if(!(other instanceof LivingEntity) || (otherCondition == null || otherCondition.test((LivingEntity)other))) {
            biEntityAction.accept(new Pair<>(entity, other));
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}

