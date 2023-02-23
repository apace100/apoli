package io.github.apace100.apoli.behavior.goal;

import io.github.apace100.apoli.Apoli;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Pair;

import java.util.EnumSet;
import java.util.function.Predicate;

public class FollowGoal extends Goal {
    protected final PathAwareEntity mob;
    protected final double speed;
    protected final double distance;
    protected final int completionRange;
    protected final Predicate<LivingEntity> predicate;
    private LivingEntity followTarget;

    public FollowGoal(PathAwareEntity mob, double speed, double distance, int completionRange, Predicate<LivingEntity> predicate) {
        this.mob = mob;
        this.speed = speed;
        this.distance = distance;
        this.completionRange = completionRange;
        this.predicate = predicate;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity livingEntity = mob.world.getClosestEntity(mob.world.getEntitiesByClass(LivingEntity.class, mob.getBoundingBox().expand(distance), predicate), TargetPredicate.createNonAttackable(), mob, mob.getX(), mob.getY(), mob.getZ());
        if (livingEntity == null) {
            return false;
        }
        if (!livingEntity.isAlive()) {
            return false;
        }
        this.followTarget = livingEntity;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return !this.mob.getNavigation().isIdle();
    }

    @Override
    public void start() {
        this.mob.getNavigation().startMovingAlong(this.mob.getNavigation().findPathTo(followTarget, completionRange), this.speed);
    }

    @Override
    public void stop() {
        this.followTarget = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity livingEntity = followTarget;
        if (livingEntity == null) {
            return;
        }
        this.mob.getLookControl().lookAt(livingEntity, 10.0f, this.mob.getMaxLookPitchChange());
        this.mob.getNavigation().startMovingAlong(this.mob.getNavigation().findPathTo(followTarget, completionRange), this.speed);
    }
}
