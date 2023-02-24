package io.github.apace100.apoli.behavior.types;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.behavior.BehaviorFactory;
import io.github.apace100.apoli.behavior.MobBehavior;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.FleeEntityGoalAccessor;
import io.github.apace100.apoli.registry.ApoliActivities;
import io.github.apace100.apoli.registry.ApoliMemoryModuleTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Pair;

import java.util.Map;
import java.util.function.Predicate;

public class FleeMobBehavior extends MobBehavior {
    private final float fleeDistance;
    private final double speed;
    private final double fastSpeed;

    public FleeMobBehavior(MobEntity mob, int priority, Predicate<Pair<LivingEntity, LivingEntity>> bientityCondition, float fleeDistance, double slowSpeed, double fastSpeed) {
        super(mob, priority, bientityCondition);
        this.fleeDistance = fleeDistance;
        this.speed = slowSpeed;
        this.fastSpeed = fastSpeed;
    }

    @Override
    public void initGoals() {
        if (!(mob instanceof PathAwareEntity) || !usesGoals()) return;
        Goal fleeGoal = new FleeEntityGoal<>((PathAwareEntity) mob, LivingEntity.class, fleeDistance, speed, fastSpeed, this::doesApply);
        ((FleeEntityGoalAccessor)fleeGoal).setWithinRangePredicate(TargetPredicate.createNonAttackable().setBaseMaxDistance(fleeDistance).setPredicate(this::doesApply));
        this.addToGoalSelector(fleeGoal);
    }

    @Override
    protected void tickMemories(LivingEntity target) {
        if (mob.getBrain().hasMemoryModule(ApoliMemoryModuleTypes.AVOID_TARGET) && mob.getBrain().getOptionalMemory(ApoliMemoryModuleTypes.AVOID_TARGET).isPresent()) {
            if (mob.getBrain().getOptionalMemory(ApoliMemoryModuleTypes.AVOID_TARGET).get() == target) {
                if (mob.getBrain().hasMemoryModule(MemoryModuleType.WALK_TARGET) && mob.squaredDistanceTo(target) < 49.0) {
                    mob.getNavigation().setSpeed(fastSpeed);
                } else {
                    mob.getNavigation().setSpeed(speed);
                }
            }
        } else if ((mob.getBrain().isMemoryInState(ApoliMemoryModuleTypes.AVOID_TARGET, MemoryModuleState.VALUE_ABSENT) || mob.getBrain().isMemoryInState(ApoliMemoryModuleTypes.AVOID_TARGET, MemoryModuleState.VALUE_PRESENT) && mob.getBrain().getOptionalMemory(ApoliMemoryModuleTypes.AVOID_TARGET).isPresent() && !doesApply(mob.getBrain().getOptionalMemory(ApoliMemoryModuleTypes.AVOID_TARGET).get()))) {
            mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            mob.getBrain().forget(MemoryModuleType.WALK_TARGET);
            mob.getBrain().remember(ApoliMemoryModuleTypes.AVOID_TARGET, target, 20L);
        }
    }

    @Override
    public boolean isPassive(LivingEntity target) {
        return doesApply(target);
    }

    @Override
    protected Map<Activity, Pair<ImmutableList<? extends Task<?>>, ImmutableList<com.mojang.datafixers.util.Pair<MemoryModuleType<?>, MemoryModuleState>>>> tasksToApply() {
        return Map.of(ApoliActivities.AVOID, new Pair<>(ImmutableList.of(GoToRememberedPositionTask.createEntityBased(ApoliMemoryModuleTypes.AVOID_TARGET, (float)speed, (int)fleeDistance, true), ForgetTask.create((m) -> MobBehavior.shouldForgetTarget((MobEntity)m, this, ApoliMemoryModuleTypes.AVOID_TARGET), ApoliMemoryModuleTypes.AVOID_TARGET)), ImmutableList.of(new com.mojang.datafixers.util.Pair<>(ApoliMemoryModuleTypes.AVOID_TARGET, MemoryModuleState.VALUE_PRESENT))));
    }

    public static BehaviorFactory<?> createFactory() {
        return new BehaviorFactory<>(Apoli.identifier("flee"),
                new SerializableData()
                        .add("priority", SerializableDataTypes.INT, 0)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("distance", SerializableDataTypes.FLOAT)
                        .add("speed", SerializableDataTypes.DOUBLE, 1.0D)
                        .addFunctionedDefault("fast_speed", SerializableDataTypes.DOUBLE, data -> data.getDouble("speed")),
                (data, mob) -> new FleeMobBehavior(mob, data.getInt("priority"), data.get("bientity_condition"), data.getFloat("distance"), data.getDouble("speed"), data.getDouble("fast_speed")));
    }
}