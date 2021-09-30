package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnEntityUsePower extends Power {

    private final Consumer<Pair<Entity, Entity>> biEntityAction;
    private final Predicate<Entity> otherCondition;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;

    public ActionOnEntityUsePower(PowerType<?> type, LivingEntity entity, Consumer<Pair<Entity, Entity>> biEntityAction, Predicate<Entity> otherCondition, Predicate<Pair<Entity, Entity>> bientityCondition) {
        super(type, entity);
        this.biEntityAction = biEntityAction;
        this.otherCondition = otherCondition;
        this.bientityCondition = bientityCondition;
    }

    public boolean shouldExecute(Entity other) {
        return (otherCondition == null || otherCondition.test( other))
            && (bientityCondition == null || bientityCondition.test(new Pair<>(entity,other)));
    }

    public ActionResult executeAction(Entity other) {
        if (otherCondition == null || otherCondition.test(other)) {
            biEntityAction.accept(new Pair<>(entity, other));
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}

