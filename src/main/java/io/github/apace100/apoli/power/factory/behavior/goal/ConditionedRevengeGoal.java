package io.github.apace100.apoli.power.factory.behavior.goal;

import io.github.apace100.apoli.mixin.RevengeGoalAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.PathAwareEntity;

import java.util.function.Predicate;

public class ConditionedRevengeGoal extends RevengeGoal {
    private final Predicate<LivingEntity> predicate;

    public ConditionedRevengeGoal(PathAwareEntity mob, Predicate<LivingEntity> predicate) {
        super(mob);
        this.predicate = predicate;
    }

    @Override
    public boolean canStart() {
        int i = this.mob.getLastAttackedTime();
        LivingEntity livingEntity = this.mob.getAttacker();
        if (i == ((RevengeGoalAccessor)this).getLastAttackedTime() || livingEntity == null) {
            return false;
        }
        if (!predicate.test(livingEntity)) {
            return false;
        }
        return this.canTrack(livingEntity, TargetPredicate.createAttackable().ignoreVisibility().ignoreDistanceScalingFactor());
    }
}
