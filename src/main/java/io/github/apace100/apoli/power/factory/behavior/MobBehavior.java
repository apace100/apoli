package io.github.apace100.apoli.power.factory.behavior;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.access.BrainTaskAddition;
import io.github.apace100.apoli.mixin.BrainAccessor;
import io.github.apace100.apoli.mixin.MobEntityAccessor;
import io.github.apace100.apoli.registry.ApoliMemoryModuleTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.TypeFilter;

import java.util.*;
import java.util.function.Predicate;

public class MobBehavior {
    private MobBehaviorFactory<?> factory;

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

    public int getPriority() {
        return priority;
    }

    public void applyGoals() {
    }

    public void applyActivities() {
        for (Map.Entry<Activity, Pair<ImmutableList<? extends Task<?>>, List<MemoryModuleType<?>>>> entry : this.tasksToApply().entrySet()) {
            List<MemoryModuleType<?>> memoryList = entry.getValue().getRight();
            memoryList.add(ApoliMemoryModuleTypes.BEHAVIOR_TARGET);
            ((BrainTaskAddition)mob.getBrain()).addToTaskList(entry.getKey(), this.priority, entry.getValue().getLeft(), ImmutableList.copyOf(memoryList));
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

    public NbtElement toTag() {
        return new NbtCompound();
    }

    public void fromTag(NbtElement tag) {

    }

    public boolean doesApply(LivingEntity target) {
        return bientityCondition == null || bientityCondition.test(new Pair<>(mob, target));
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

    protected Map<Activity, Pair<ImmutableList<? extends Task<?>>, List<MemoryModuleType<?>>>> tasksToApply() {
        return new HashMap<>();
    }

    public void baseTick() {
        this.tick();
        List<? extends LivingEntity> applicableEntities = (((ServerWorld)mob.getWorld()).getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), this::doesApply));
        LivingEntity closestTarget = mob.getWorld().getClosestEntity(applicableEntities, TargetPredicate.createNonAttackable(), mob, mob.getX(), mob.getY(), mob.getZ());
        if (closestTarget == null && previousTarget != null || closestTarget != previousTarget) {
            this.resetAttackTargets();
            this.onRemoved();
        }

        if (closestTarget != null) {
            if (closestTarget != previousTarget) {
                this.onAdded();
            }
        }

        previousTarget = closestTarget;
    }

    protected boolean hasMemoryModule(MemoryModuleType<?> type) {
        return mob.getBrain().isMemoryInState(type, MemoryModuleState.REGISTERED) || mob.getBrain().isMemoryInState(type, MemoryModuleState.VALUE_PRESENT) || mob.getBrain().isMemoryInState(type, MemoryModuleState.VALUE_ABSENT);
    }

    protected void resetAttackTargets() {
        mob.getBrain().forget(MemoryModuleType.ANGRY_AT);
        mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
        mob.getBrain().forget(ApoliMemoryModuleTypes.ATTACK_TARGET);

        if (mob.getTarget() != null || mob instanceof Angerable angerable && angerable.getAngryAt() != null) {
            if (mob instanceof Angerable) {
                ((Angerable) mob).stopAnger();
            }
            mob.setTarget(null);
        }
    }

    protected void addToGoalSelector(Goal goal) {
        this.addToGoalSelector(0, goal);
    }

    protected void addToGoalSelector(int priorityOffset, Goal goal) {
        ((MobEntityAccessor)mob).getGoalSelector().add(this.priority + priorityOffset, goal);
        modifiedGoalSelectorGoals.add(goal);
    }

    protected void addToTargetSelector(Goal goal) {
        this.addToTargetSelector(0, goal);
    }

    protected void addToTargetSelector(int priorityOffset, Goal goal) {
        ((MobEntityAccessor)mob).getTargetSelector().add(this.priority + priorityOffset, goal);
        modifiedTargetSelectorGoals.add(goal);
    }

    public MobBehaviorFactory<?> getFactory() {
        return this.factory;
    }

    public void setFactory(MobBehaviorFactory<?> factory) {
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

    protected static SingleTickTask<MobEntity> taskWithBehaviorTargetTask(SingleTickTask<MobEntity> task, Predicate<LivingEntity> behaviorTargetPredicate) {
        return Tasks.weighted(List.of(com.mojang.datafixers.util.Pair.of(createBehaviorTargetTask(behaviorTargetPredicate), 0), com.mojang.datafixers.util.Pair.of(task, 1)), CompositeTask.Order.ORDERED, CompositeTask.RunMode.TRY_ALL);
    }

    private static SingleTickTask<MobEntity> createBehaviorTargetTask(Predicate<LivingEntity> predicate) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(ApoliMemoryModuleTypes.BEHAVIOR_TARGET)).apply(context, (behaviorTarget) -> (world, entity, time) -> {
            List<? extends LivingEntity> applicableEntities = (((ServerWorld)entity.getWorld()).getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), predicate));
            LivingEntity closestTarget = entity.getWorld().getClosestEntity(applicableEntities, TargetPredicate.createNonAttackable(), entity, entity.getX(), entity.getY(), entity.getZ());

            behaviorTarget.remember(closestTarget, 1L);

            return true;
        }));
    }
}