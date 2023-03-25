package io.github.apace100.apoli.power.factory.behavior.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.mob.MobEntity;

import java.util.function.Predicate;

public class ConditionedLookAtEntityGoal extends LookAtEntityGoal {

    public ConditionedLookAtEntityGoal(MobEntity mob, Predicate<LivingEntity> predicate) {
        super(mob, LivingEntity.class, 0.0F);
        this.targetPredicate.setPredicate(predicate);
    }
}
