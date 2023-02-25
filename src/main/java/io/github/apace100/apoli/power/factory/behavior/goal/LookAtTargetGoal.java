package io.github.apace100.apoli.power.factory.behavior.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;

import java.util.EnumSet;
import java.util.function.Predicate;

public class LookAtTargetGoal extends Goal {
    protected final PathAwareEntity mob;
    protected final Predicate<LivingEntity> predicate;
    private LivingEntity lookTarget;

    public LookAtTargetGoal(PathAwareEntity mob, Predicate<LivingEntity> predicate) {
        this.mob = mob;
        this.predicate = predicate;
        this.setControls(EnumSet.of(Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity livingEntity = mob.world.getClosestEntity((((ServerWorld)mob.world).getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), predicate)), TargetPredicate.createNonAttackable(), mob, mob.getX(), mob.getY(), mob.getZ());
        if (livingEntity == null) {
            return false;
        }
        if (!livingEntity.isAlive()) {
            return false;
        }
        this.lookTarget = livingEntity;
        return true;
    }

    @Override
    public void start() {
        this.mob.getLookControl().lookAt(lookTarget);
    }

    @Override
    public void stop() {
        this.lookTarget = null;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity livingEntity = lookTarget;
        if (livingEntity == null) {
            return;
        }
        this.mob.getLookControl().lookAt(livingEntity);
    }
}
