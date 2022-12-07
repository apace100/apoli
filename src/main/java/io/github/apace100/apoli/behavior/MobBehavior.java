package io.github.apace100.apoli.behavior;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.ModifiableMobWithGoals;
import io.github.apace100.apoli.mixin.BrainAccessor;
import io.github.apace100.apoli.mixin.MobEntityAccessor;
import io.github.apace100.apoli.power.ModifyMobBehaviorPower;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Pair;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class MobBehavior {
    private BehaviorFactory<?> factory;

    protected int priority;
    protected Predicate<Pair<LivingEntity, MobEntity>> mobRelatedPredicates;
    protected Predicate<LivingEntity> entityRelatedPredicates;

    public MobBehavior(int priority) {
        this.priority = priority;
    }

    private BehaviorFactory<?> getFactory() {
        return this.factory;
    }

    public void setFactory(BehaviorFactory<?> factory) {
        this.factory = factory;
    }

    public void initGoals(MobEntity mob) {
    }

    public void removeGoals(MobEntity mob) {
        ((ModifiableMobWithGoals)mob).getModifiedTargetSelectorGoals().stream().filter(pair -> pair.getLeft() == this).forEach(pair -> ((MobEntityAccessor)mob).getTargetSelector().remove(pair.getRight()));
        ((ModifiableMobWithGoals)mob).getModifiedTargetSelectorGoals().removeIf(pair -> pair.getLeft() == this);
        ((ModifiableMobWithGoals)mob).getModifiedGoalSelectorGoals().stream().filter(pair -> pair.getLeft() == this).forEach(pair -> ((MobEntityAccessor)mob).getGoalSelector().remove(pair.getRight()));
        ((ModifiableMobWithGoals)mob).getModifiedGoalSelectorGoals().removeIf(pair -> pair.getLeft() == this);
    }

    public void removeTasks(MobEntity mob) {
        if (!usesBrain(mob)) return;
        for (Activity activity : this.activitiesToApply()) {
            ((BrainAccessor)mob.getBrain()).getPossibleActivities().remove(activity);
        }
    }

    protected Set<Activity> activitiesToApply() {
        return Set.of();
    }

    public boolean hasAppliedGoals(MobEntity mob) {
        return ((ModifiableMobWithGoals)mob).getModifiedTargetSelectorGoals().stream().filter(pair -> pair.getLeft() == this).toList().size() > 0 || ((ModifiableMobWithGoals)mob).getModifiedGoalSelectorGoals().stream().filter(pair -> pair.getLeft() == this).toList().size() > 0;
    }

    public boolean hasAppliedActivities(MobEntity mob) {
        return activitiesToApply().stream().allMatch(activity -> mob.getBrain().hasActivity(activity));
    }

    public boolean isPassive(MobEntity mob, LivingEntity target) {
        return false;
    }

    public boolean isHostile(MobEntity mob, LivingEntity target) {
        return false;
    }

    public void onMobDamage(MobEntity mob, Entity attacker) {

    }

    public void tick(MobEntity mob) {
    }

    protected void addToGoalSelector(MobEntity mob, Goal goal) {
        ((MobEntityAccessor)mob).getGoalSelector().add(this.priority, goal);
        ((ModifiableMobWithGoals)mob).getModifiedGoalSelectorGoals().add(new Pair<>(this, goal));
    }

    protected void addToTargetSelector(MobEntity mob, Goal goal) {
        ((MobEntityAccessor)mob).getTargetSelector().add(this.priority, goal);
        ((ModifiableMobWithGoals)mob).getModifiedTargetSelectorGoals().add(new Pair<>(this, goal));
    }

    public void addMobRelatedPredicate(Predicate<Pair<LivingEntity, MobEntity>> predicate) {
        mobRelatedPredicates = mobRelatedPredicates == null ? predicate : mobRelatedPredicates.and(predicate);
    }

    public void addEntityRelatedPredicate(Predicate<LivingEntity> predicate) {
        entityRelatedPredicates = entityRelatedPredicates == null ? predicate : entityRelatedPredicates.and(predicate);
    }

    protected void setToDataInstance(SerializableData.Instance dataInstance) {
        dataInstance.set("priority", this.priority);
    }

    public void send(PacketByteBuf buffer) {
        BehaviorFactory<?> factory = getFactory();
        buffer.writeIdentifier(factory.getSerializerId());
        SerializableData data = factory.getData();
        SerializableData.Instance dataInstance = data.new Instance();
        this.setToDataInstance(dataInstance);
        data.write(buffer, dataInstance);
    }

    public static boolean usesBrain(MobEntity mob) {
        return !((BrainAccessor)mob.getBrain()).getCoreActivities().isEmpty();
    }
}