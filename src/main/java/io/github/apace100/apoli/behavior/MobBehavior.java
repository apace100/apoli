package io.github.apace100.apoli.behavior;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.access.BrainTaskAddition;
import io.github.apace100.apoli.access.ModifiableMobWithGoals;
import io.github.apace100.apoli.mixin.BrainAccessor;
import io.github.apace100.apoli.mixin.MobEntityAccessor;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Pair;
import org.spongepowered.include.com.google.common.collect.Maps;

import java.util.*;
import java.util.function.Predicate;

public class MobBehavior {
    private BehaviorFactory<?> factory;

    protected int priority;
    protected Set<MobEntity> activeEntities = new HashSet<>();
    protected Predicate<Pair<LivingEntity, MobEntity>> biEntityPredicate;
    private final List<LivingEntity> potentialTargets = new ArrayList<>();

    public MobBehavior(int priority) {
        this.priority = priority;
    }

    public void initGoals(MobEntity mob) {
    }

    public boolean isPassive(MobEntity mob, LivingEntity target) {
        return false;
    }

    public boolean isHostile(MobEntity mob, LivingEntity target) {
        return false;
    }

    public void onAttacked(MobEntity mob, Entity attacker) {
    }

    public void onAdded(MobEntity mob) {

    }

    public void onRemoved(MobEntity mob) {

    }

    public void tick(MobEntity mob) {
    }

    protected void tickMemories(MobEntity mob, LivingEntity other) {

    }

    public void removeGoals(MobEntity mob) {
        if (this.isHostile(mob, mob.getTarget())) {
            if (mob instanceof Angerable) {
                ((Angerable) mob).stopAnger();
            }
            mob.setTarget(null);
        }
        ((ModifiableMobWithGoals)mob).getModifiedTargetSelectorGoals().stream().filter(pair -> pair.getLeft() == this).forEach(pair -> ((MobEntityAccessor)mob).getTargetSelector().remove(pair.getRight()));
        ((ModifiableMobWithGoals)mob).getModifiedTargetSelectorGoals().removeIf(pair -> pair.getLeft() == this);
        ((ModifiableMobWithGoals)mob).getModifiedGoalSelectorGoals().stream().filter(pair -> pair.getLeft() == this).forEach(pair -> ((MobEntityAccessor)mob).getGoalSelector().remove(pair.getRight()));
        ((ModifiableMobWithGoals)mob).getModifiedGoalSelectorGoals().removeIf(pair -> pair.getLeft() == this);
    }

    public void removeTasks(MobEntity mob) {
        if (!usesBrain(mob) || this.tasksToApply().isEmpty()) return;
        for (Activity activity : this.tasksToApply().keySet()) {
            ((BrainAccessor)mob.getBrain()).getPossibleActivities().remove(activity);
        }
        activeEntities.remove(mob);
    }

    protected Map<Activity, Pair<ImmutableList<? extends Task<?>>, ImmutableList<com.mojang.datafixers.util.Pair<MemoryModuleType<?>, MemoryModuleState>>>> tasksToApply() {
        return Maps.newHashMap();
    }

    public boolean hasAppliedGoals(MobEntity mob) {
        return ((ModifiableMobWithGoals)mob).getModifiedTargetSelectorGoals().stream().filter(pair -> pair.getLeft() == this).toList().size() > 0 || ((ModifiableMobWithGoals)mob).getModifiedGoalSelectorGoals().stream().filter(pair -> pair.getLeft() == this).toList().size() > 0;
    }

    public boolean hasAppliedTasks(MobEntity mob) {
        if (!MobBehavior.usesBrain(mob)) {
            return false;
        }
        boolean value = true;
        for (Activity activity : this.tasksToApply().keySet()) {
            if (!mob.getBrain().hasActivity(activity))
                value = false;
        }
        return value;
    }

    public boolean isActive(MobEntity mob) {
        return activeEntities.contains(mob);
    }

    public void tickTasks(MobEntity mob) {
        if (!usesBrain(mob) || this.tasksToApply().isEmpty()) return;
        this.potentialTargets.removeIf(potentialTarget -> !biEntityPredicate.test(new Pair<>(potentialTarget, mob)));
        LivingEntity other = mob.world.getClosestEntity(this.potentialTargets, TargetPredicate.createNonAttackable(), mob, mob.getX(), mob.getY(), mob.getZ());
        if (potentialTargets.isEmpty() || other == null) {
            this.removeTasks(mob);
        } else {
            if (!this.hasAppliedTasks(mob)) {
                for (Map.Entry<Activity, Pair<ImmutableList<? extends Task<?>>, ImmutableList<com.mojang.datafixers.util.Pair<MemoryModuleType<?>, MemoryModuleState>>>> entry : this.tasksToApply().entrySet()) {
                    ((BrainTaskAddition)mob.getBrain()).addToTaskList(entry.getKey(), this.priority, entry.getValue().getLeft(), entry.getValue().getRight());
                }
            }
            tickMemories(mob, other);
        }
    }

    public void addToPotentialTargets(LivingEntity entity) {
        this.potentialTargets.add(entity);
    }

    public boolean isPotentialTarget(LivingEntity entity) {
        return this.potentialTargets.contains(entity);
    }

    protected void addToGoalSelector(MobEntity mob, Goal goal) {
        ((MobEntityAccessor)mob).getGoalSelector().add(this.priority, goal);
        ((ModifiableMobWithGoals)mob).getModifiedGoalSelectorGoals().add(new Pair<>(this, goal));
    }

    protected void addToTargetSelector(MobEntity mob, Goal goal) {
        ((MobEntityAccessor)mob).getTargetSelector().add(this.priority, goal);
        ((ModifiableMobWithGoals)mob).getModifiedTargetSelectorGoals().add(new Pair<>(this, goal));
    }

    public void addBiEntityPredicate(Predicate<Pair<LivingEntity, MobEntity>> predicate) {
        biEntityPredicate = biEntityPredicate == null ? predicate : biEntityPredicate.and(predicate);
    }

    protected void setToDataInstance(SerializableData.Instance dataInstance) {
        dataInstance.set("priority", this.priority);
    }

    private BehaviorFactory<?> getFactory() {
        return this.factory;
    }

    public void setFactory(BehaviorFactory<?> factory) {
        this.factory = factory;
    }

    public void write(PacketByteBuf buffer) {
        BehaviorFactory<?> factory = getFactory();
        buffer.writeIdentifier(factory.getSerializerId());
        SerializableData data = factory.getData();
        SerializableData.Instance dataInstance = data.new Instance();
        this.setToDataInstance(dataInstance);
        data.write(buffer, dataInstance);
    }

    public static boolean usesGoals(MobEntity mob) {
        return !((MobEntityAccessor) mob).getGoalSelector().getGoals().isEmpty() || !((MobEntityAccessor) mob).getTargetSelector().getGoals().isEmpty();
    }

    public static boolean usesBrain(MobEntity mob) {
        return !((BrainAccessor)mob.getBrain()).getCoreActivities().isEmpty();
    }

    public static boolean shouldForgetPowerHolder(LivingEntity entity, MobBehavior behavior, MemoryModuleType<LivingEntity> memoryModuleType) {
        if (!(entity instanceof MobEntity mob)) {
            return true;
        }
        if (!behavior.isActive(mob)) {
            return false;
        }
        Brain<?> brain = mob.getBrain();
        if (!brain.hasMemoryModule(memoryModuleType)) {
            return true;
        }
        if (brain.getOptionalMemory(memoryModuleType).isPresent()) {
            LivingEntity livingEntity = brain.getOptionalMemory(memoryModuleType).get();
            return !behavior.biEntityPredicate.test(new Pair<>(livingEntity, mob));
        }

        return true;
    }
}