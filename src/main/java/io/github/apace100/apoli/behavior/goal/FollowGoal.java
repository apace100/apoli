package io.github.apace100.apoli.behavior.goal;

import io.github.apace100.apoli.mixin.ServerWorldAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class FollowGoal extends Goal {
    protected final PathAwareEntity mob;
    protected final double speed;
    protected final int completionRange;
    protected final Predicate<Pair<LivingEntity, MobEntity>> predicate;
    private Path path;
    private int updateCountdownTicks;
    private long lastUpdateTime;

    public FollowGoal(PathAwareEntity mob, double speed, int completionRange, Predicate<Pair<LivingEntity, MobEntity>> predicate) {
        this.mob = mob;
        this.speed = speed;
        this.completionRange = completionRange;
        this.predicate = predicate;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        long l = this.mob.world.getTime();
        if (l - this.lastUpdateTime < 20L) {
            return false;
        }
        this.lastUpdateTime = l;
        List<LivingEntity> potentialTargets = new ArrayList<>();
        for (Entity entity : ((ServerWorldAccessor)mob.world).getEntityManager().getLookup().iterate()) {
            if (entity instanceof LivingEntity living && predicate.test(new Pair<>(living, mob))) {
                potentialTargets.add(living);
            }
        }
        LivingEntity livingEntity = mob.world.getClosestEntity(potentialTargets, TargetPredicate.createNonAttackable(), mob, mob.getX(), mob.getY(), mob.getZ());
        if (livingEntity == null) {
            return false;
        }
        if (!livingEntity.isAlive()) {
            return false;
        }
        if (!predicate.test(new Pair<>(livingEntity, mob))) {
            return false;
        }
        this.path = this.mob.getNavigation().findPathTo(livingEntity, completionRange);
        return true;
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) {
            return false;
        }
        if (!livingEntity.isAlive()) {
            return false;
        }
        if (!predicate.test(new Pair<>(livingEntity, mob))) {
            return false;
        }
        if (!this.mob.isInWalkTargetRange(livingEntity.getBlockPos())) {
            return false;
        }
        return !(livingEntity instanceof PlayerEntity) || !livingEntity.isSpectator();
    }

    @Override
    public void start() {
        this.mob.getNavigation().startMovingAlong(this.path, this.speed);
        this.updateCountdownTicks = 0;
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) {
            return;
        }
        this.mob.getLookControl().lookAt(livingEntity, 30.0f, 30.0f);
        if (--this.updateCountdownTicks > 0) {
            return;
        }
        this.updateCountdownTicks = this.getTickCount(10);
        this.mob.getNavigation().startMovingTo(livingEntity, this.speed);
    }
}
