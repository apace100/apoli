package io.github.apace100.apoli.power.factory.behavior.goal;

import io.github.apace100.apoli.mixin.LookAtEntityGoalAccessor;
import io.github.apace100.apoli.mixin.MobEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;
import java.util.function.Predicate;

public class ConditionedLookAtEntityGoal extends LookAtEntityGoal {
    private boolean active;

    public ConditionedLookAtEntityGoal(MobEntity mob, Predicate<LivingEntity> predicate) {
        super(mob, LivingEntity.class, 16.0F, 1.0F);
        this.targetPredicate.setPredicate(predicate);
        this.setControls(EnumSet.of(Control.LOOK));
    }

    @Override
    public boolean canStart() {
        this.target = this.mob.getWorld().getClosestEntity(this.mob.getWorld().getEntitiesByClass(this.targetType, this.mob.getBoundingBox().expand(this.range, 3.0, this.range), livingEntity -> true), this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        if (this.target != null) {
            this.active = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldContinue() {
        if (!(this.target instanceof LivingEntity)) {
            this.target = null;
            this.active = false;
            return false;
        }
        if (!this.target.isAlive()) {
            this.target = null;
            return false;
        }
        if (!this.targetPredicate.test(this.mob, (LivingEntity) this.target)) {
            this.target = null;
            return false;
        }
        return ((LookAtEntityGoalAccessor)this).getLookTime() > 0;
    }

    public boolean isActive() {
        return active;
    }

    public Entity getTarget() {
        return target;
    }

}
