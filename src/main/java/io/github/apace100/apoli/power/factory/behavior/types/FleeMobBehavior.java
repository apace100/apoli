package io.github.apace100.apoli.power.factory.behavior.types;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.behavior.MobBehaviorFactory;
import io.github.apace100.apoli.power.factory.behavior.MobBehavior;
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
    public void applyGoals() {
        if (!(mob instanceof PathAwareEntity) || !usesGoals()) return;
        Goal fleeGoal = new FleeEntityGoal<>((PathAwareEntity) mob, LivingEntity.class, fleeDistance, speed, fastSpeed, this::doesApply);
        ((FleeEntityGoalAccessor)fleeGoal).setWithinRangePredicate(TargetPredicate.createNonAttackable().setBaseMaxDistance(fleeDistance).setPredicate(this::doesApply));
        this.addToGoalSelector(fleeGoal);
    }

    @Override
    public boolean isPassive(LivingEntity target) {
        return doesApply(target);
    }

    @Override
    protected Map<Activity, Pair<ImmutableList<? extends Task<?>>, ImmutableList<com.mojang.datafixers.util.Pair<MemoryModuleType<?>, MemoryModuleState>>>> tasksToApply() {
        return Map.of(ApoliActivities.AVOID, new Pair<>(ImmutableList.of(MobBehavior.taskWithBehaviorTargetTask(createRememberTask(this::doesApply, speed, fastSpeed), this::doesApply), GoToRememberedPositionTask.createEntityBased(ApoliMemoryModuleTypes.AVOID_TARGET, (float)speed, (int)fleeDistance, true), ForgetTask.create((m) -> MobBehavior.shouldForgetTarget((MobEntity)m, this, ApoliMemoryModuleTypes.AVOID_TARGET), ApoliMemoryModuleTypes.AVOID_TARGET)), ImmutableList.of(com.mojang.datafixers.util.Pair.of(ApoliMemoryModuleTypes.AVOID_TARGET, MemoryModuleState.REGISTERED))));
    }

    public static SingleTickTask<MobEntity> createRememberTask(Predicate<LivingEntity> predicate, double speed, double fastSpeed) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(ApoliMemoryModuleTypes.BEHAVIOR_TARGET), context.queryMemoryOptional(ApoliMemoryModuleTypes.AVOID_TARGET), context.queryMemoryOptional(MemoryModuleType.WALK_TARGET)).apply(context, (behaviorTarget, avoidTarget, walkTarget) -> (world, entity, time) -> {
            if (context.getOptionalValue(avoidTarget).isPresent()) {
                if (context.getOptionalValue(avoidTarget).get() == context.getValue(behaviorTarget)) {
                    if (entity.squaredDistanceTo(context.getValue(behaviorTarget)) < 49.0) {
                        entity.getNavigation().setSpeed(fastSpeed);
                    } else {
                        entity.getNavigation().setSpeed(speed);
                    }
                    return true;
                }
            } else if ((context.getOptionalValue(avoidTarget).isEmpty() || context.getOptionalValue(avoidTarget).isPresent() && !predicate.test(context.getOptionalValue(avoidTarget).get()))) {
                entity.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
                entity.getBrain().forget(MemoryModuleType.WALK_TARGET);
                entity.getBrain().remember(ApoliMemoryModuleTypes.AVOID_TARGET, context.getValue(behaviorTarget), 20L);
                return true;
            }
            return false;
        }));
    }

    public static MobBehaviorFactory<?> createFactory() {
        return new MobBehaviorFactory<>(Apoli.identifier("flee"),
                new SerializableData()
                        .add("priority", SerializableDataTypes.INT, 0)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("distance", SerializableDataTypes.FLOAT)
                        .add("speed", SerializableDataTypes.DOUBLE, 1.0D)
                        .addFunctionedDefault("fast_speed", SerializableDataTypes.DOUBLE, data -> data.getDouble("speed")),
                (data, mob) -> new FleeMobBehavior(mob, data.getInt("priority"), data.get("bientity_condition"), data.getFloat("distance"), data.getDouble("speed"), data.getDouble("fast_speed")));
    }
}