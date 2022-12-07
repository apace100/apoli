package io.github.apace100.apoli.behavior.ai;

import io.github.apace100.apoli.mixin.FleeEntityGoalAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.PathAwareEntity;

import java.util.function.Predicate;

public class NonAttackableFleeEntityGoal<T extends LivingEntity>
extends FleeEntityGoal<T> {

    public NonAttackableFleeEntityGoal(PathAwareEntity mob, Class<T> fleeFromType, float distance, double slowSpeed, double fastSpeed, Predicate<LivingEntity> inclusionSelector) {
        super(mob, fleeFromType, distance, slowSpeed, fastSpeed, inclusionSelector);
        ((FleeEntityGoalAccessor)this).setWithinRangePredicate(TargetPredicate.createNonAttackable().setBaseMaxDistance(distance).setPredicate(inclusionSelector));
    }
}