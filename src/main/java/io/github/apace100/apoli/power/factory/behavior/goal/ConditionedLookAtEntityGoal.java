package io.github.apace100.apoli.power.factory.behavior.goal;

import io.github.apace100.apoli.mixin.LookAtEntityGoalAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Predicate;

public class ConditionedLookAtEntityGoal extends LookAtEntityGoal {

    public ConditionedLookAtEntityGoal(MobEntity mob, Predicate<LivingEntity> predicate) {
        super(mob, LivingEntity.class, 16.0F, 1.0F);
        this.targetPredicate.setPredicate(predicate);
    }

    @Override
    public boolean canStart() {
        this.target = this.targetType == PlayerEntity.class ? this.mob.getWorld().getClosestPlayer(this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ()) : this.mob.getWorld().getClosestEntity(this.mob.getWorld().getEntitiesByClass(this.targetType, this.mob.getBoundingBox().expand(this.range, 3.0, this.range), livingEntity -> true), this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        return this.target != null;
    }

    @Override
    public boolean shouldContinue() {
        if (!this.target.isAlive()) {
            return false;
        }
        return ((LookAtEntityGoalAccessor)this).getLookTime() > 0;
    }
}
