package io.github.apace100.apoli.behavior;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.access.BrainTaskAddition;
import io.github.apace100.apoli.mixin.BrainAccessor;
import io.github.apace100.apoli.mixin.MobEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.TypeFilter;
import org.spongepowered.include.com.google.common.collect.Maps;

import java.util.*;
import java.util.function.Predicate;

public class MobBehavior {
    private BehaviorFactory<?> factory;

    protected final MobEntity mob;
    protected final int priority;
    private final Predicate<Pair<LivingEntity, LivingEntity>> bientityCondition;
    private LivingEntity previousTarget;

    private final List<Goal> modifiedGoalSelectorGoals = new ArrayList<>();
    private final List<Goal> modifiedTargetSelectorGoals = new ArrayList<>();


    public MobBehavior(MobEntity mob, int priority, Predicate<Pair<LivingEntity, LivingEntity>> bientityCondition) {
        this.mob = mob;
        this.priority = priority;
        this.bientityCondition = bientityCondition;
    }

    public void initGoals() {
    }

    public void initActivities() {
        for (Map.Entry<Activity, Pair<ImmutableList<? extends Task<?>>, ImmutableList<com.mojang.datafixers.util.Pair<MemoryModuleType<?>, MemoryModuleState>>>> entry : this.tasksToApply().entrySet()) {
            ((BrainTaskAddition)mob.getBrain()).addToTaskList(entry.getKey(), this.priority, entry.getValue().getLeft(), entry.getValue().getRight());
        }
    }

    public boolean isPassive(LivingEntity target) {
        return false;
    }

    public void onAttacked(Entity attacker) {
    }

    public void onAdded() {

    }

    public void onRemoved() {

    }

    public void tick() {
    }

    public boolean doesApply(LivingEntity target) {
        return bientityCondition == null || bientityCondition.test(new Pair<>(mob, target));
    }

    protected void tickMemories(LivingEntity target) {

    }

    public void removeGoals() {
        modifiedTargetSelectorGoals.forEach(goal -> ((MobEntityAccessor)mob).getTargetSelector().remove(goal));
        modifiedGoalSelectorGoals.forEach(goal -> ((MobEntityAccessor)mob).getGoalSelector().remove(goal));
    }

    public void removeTasks() {
        if (!usesBrain() || this.tasksToApply().isEmpty()) return;
        for (Activity activity : this.tasksToApply().keySet()) {
            ((BrainAccessor)mob.getBrain()).getPossibleActivities().remove(activity);
        }
    }

    protected Map<Activity, Pair<ImmutableList<? extends Task<?>>, ImmutableList<com.mojang.datafixers.util.Pair<MemoryModuleType<?>, MemoryModuleState>>>> tasksToApply() {
        return Maps.newHashMap();
    }

    public void baseTick() {
        this.tick();
        List<? extends LivingEntity> applicableEntities = (((ServerWorld)mob.world).getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), this::doesApply));
        LivingEntity closestTarget = mob.world.getClosestEntity(applicableEntities, TargetPredicate.createNonAttackable(), mob, mob.getX(), mob.getY(), mob.getZ());;
        if (closestTarget == null && previousTarget != null || closestTarget != previousTarget) {
            this.onRemoved();
        }

        if (closestTarget != null) {
            if (closestTarget != previousTarget) {
                this.onAdded();
            }
            tickMemories(closestTarget);
        }

        previousTarget = closestTarget;
    }

    protected void addToGoalSelector(Goal goal) {
        ((MobEntityAccessor)mob).getGoalSelector().add(this.priority, goal);
        modifiedGoalSelectorGoals.add(goal);
    }

    protected void addToTargetSelector(Goal goal) {
        ((MobEntityAccessor)mob).getTargetSelector().add(this.priority, goal);
        modifiedTargetSelectorGoals.add(goal);
    }

    public BehaviorFactory<?> getFactory() {
        return this.factory;
    }

    public void setFactory(BehaviorFactory<?> factory) {
        this.factory = factory;
    }

    public boolean usesGoals() {
        return !((MobEntityAccessor) mob).getGoalSelector().getGoals().isEmpty() || !((MobEntityAccessor) mob).getTargetSelector().getGoals().isEmpty();
    }

    public boolean usesBrain() {
        return !((BrainAccessor)mob.getBrain()).getCoreActivities().isEmpty();
    }

    public static boolean shouldForgetTarget(MobEntity actor, MobBehavior behavior, MemoryModuleType<LivingEntity> memoryModuleType) {
        Brain<?> brain = actor.getBrain();
        if (!brain.hasMemoryModule(memoryModuleType)) {
            return true;
        }
        if (brain.getOptionalMemory(memoryModuleType).isPresent()) {
            LivingEntity target = brain.getOptionalMemory(memoryModuleType).get();
            return !behavior.doesApply(target);
        }

        return true;
    }
}