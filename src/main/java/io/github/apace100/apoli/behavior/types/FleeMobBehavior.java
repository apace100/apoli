package io.github.apace100.apoli.behavior.types;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.BrainTaskAddition;
import io.github.apace100.apoli.behavior.BehaviorFactory;
import io.github.apace100.apoli.behavior.MobBehavior;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.mixin.BrainAccessor;
import io.github.apace100.apoli.power.ModifyMobBehaviorPower;
import io.github.apace100.apoli.registry.ApoliActivities;
import io.github.apace100.apoli.registry.ApoliMemoryModuleTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.ForgetTask;
import net.minecraft.entity.ai.brain.task.GoToRememberedPositionTask;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Pair;

import java.util.Optional;
import java.util.Set;

public class FleeMobBehavior extends MobBehavior {
    private final float fleeDistance;
    private final double slowSpeed;
    private final double fastSpeed;

    public FleeMobBehavior(int priority, float fleeDistance, double slowSpeed, double fastSpeed) {
        super(priority);
        this.fleeDistance = fleeDistance;
        this.slowSpeed = slowSpeed;
        this.fastSpeed = fastSpeed;
    }

    @Override
    protected void setToDataInstance(SerializableData.Instance dataInstance) {
        super.setToDataInstance(dataInstance);
        dataInstance.set("distance", this.fleeDistance);
        dataInstance.set("slow_speed", this.slowSpeed);
        dataInstance.set("fast_speed", this.fastSpeed);
    }

    @Override
    public void initGoals(MobEntity mob) {
        if (!(mob instanceof PathAwareEntity) || usesBrain(mob)) return;
        Goal fleeGoal = new FleeEntityGoal<>((PathAwareEntity) mob, LivingEntity.class, fleeDistance, slowSpeed, fastSpeed, entity -> mobRelatedPredicates.test(new Pair<>(entity, mob)) && entityRelatedPredicates.test(entity));
        this.addToGoalSelector(mob, fleeGoal);
    }

    @Override
    public void tick(MobEntity mob) {
        if (!usesBrain(mob)) return;
        Optional<Entity> powerHolder = mob.world.getOtherEntities(mob, mob.getBoundingBox().expand(this.fleeDistance), entity -> entity instanceof LivingEntity living && mobRelatedPredicates.test(new Pair<>(living, mob)) && entityRelatedPredicates.test(living)).stream().findFirst();
        if (powerHolder.isEmpty()) {
            this.removeTasks(mob);
        } else {
            if (!this.hasAppliedActivities(mob)) {
                ((BrainAccessor)mob.getBrain()).getPossibleActivities().add(ApoliActivities.AVOID);
                ((BrainTaskAddition)mob.getBrain()).addToTaskList(ApoliActivities.AVOID, this.priority, ImmutableList.of(GoToRememberedPositionTask.toEntity(ApoliMemoryModuleTypes.AVOID_TARGET, (float) this.slowSpeed, Math.round(this.fleeDistance), true), new ForgetTask<>(FleeMobBehavior::shouldForgetAvoidTask, ApoliMemoryModuleTypes.AVOID_TARGET)), ImmutableList.of(new com.mojang.datafixers.util.Pair<>(ApoliMemoryModuleTypes.AVOID_TARGET, MemoryModuleState.VALUE_PRESENT)), mob);
            }
            if (!mob.getBrain().hasMemoryModule(ApoliMemoryModuleTypes.AVOID_TARGET)) {
                mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
                mob.getBrain().forget(MemoryModuleType.WALK_TARGET);
                mob.getBrain().remember(ApoliMemoryModuleTypes.AVOID_TARGET, (LivingEntity)powerHolder.get(), mob.age);
            }
        }
    }

    private static boolean shouldForgetAvoidTask(MobEntity mob) {
        Brain<?> brain = mob.getBrain();
        if (!brain.hasMemoryModule(ApoliMemoryModuleTypes.AVOID_TARGET)) {
            return true;
        }
        if (brain.getOptionalMemory(ApoliMemoryModuleTypes.AVOID_TARGET).isPresent()) {
            LivingEntity livingEntity = brain.getOptionalMemory(ApoliMemoryModuleTypes.AVOID_TARGET).get();
            return PowerHolderComponent.getPowers(livingEntity, ModifyMobBehaviorPower.class).stream().anyMatch(power -> power.getMobBehavior() instanceof FleeMobBehavior && !power.doesApply(livingEntity, mob));
        }

        return true;
    }

    @Override
    protected Set<Activity> activitiesToApply() {
        return Set.of(ApoliActivities.AVOID);
    }

    @Override
    public boolean isPassive(MobEntity mob, LivingEntity target) {
        return true;
    }

    public static BehaviorFactory<?> createFactory() {
        return new BehaviorFactory<>(Apoli.identifier("flee"),
                new SerializableData()
                        .add("priority", SerializableDataTypes.INT, 0)
                        .add("distance", SerializableDataTypes.FLOAT)
                        .add("slow_speed", SerializableDataTypes.DOUBLE)
                        .addFunctionedDefault("fast_speed", SerializableDataTypes.DOUBLE, data -> data.getDouble("slow_speed")),
                data -> new FleeMobBehavior(data.getInt("priority"), data.getFloat("distance"), data.getDouble("slow_speed"), data.getDouble("fast_speed")));
    }
}