package io.github.apace100.apoli.power.factory.behavior.types;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.behavior.BehaviorFactory;
import io.github.apace100.apoli.power.factory.behavior.MobBehavior;
import io.github.apace100.apoli.power.factory.behavior.goal.FollowGoal;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliActivities;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.function.Predicate;

public class FollowMobBehavior extends MobBehavior {
    private final float speed;
    private final float distance;
    private final int completionRange;

    public FollowMobBehavior(MobEntity mob, int priority, Predicate<Pair<LivingEntity, LivingEntity>> bientityCondition, float speed, float distance, int completionRange) {
        super(mob, priority, bientityCondition);
        this.speed = speed;
        this.distance = distance;
        this.completionRange = completionRange;
    }

    @Override
    public void initGoals() {
        if (!(mob instanceof PathAwareEntity pathAware) || !usesGoals()) return;
        this.addToGoalSelector(new FollowGoal(pathAware, speed, distance, completionRange, this::doesApply));
    }

    @Override
    protected void tickMemories(LivingEntity target) {
        if ((mob.getBrain().isMemoryInState(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT) || mob.getBrain().isMemoryInState(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_PRESENT) && mob.getBrain().getOptionalMemory(MemoryModuleType.LOOK_TARGET).isPresent() && mob.getBrain().getOptionalMemory(MemoryModuleType.LOOK_TARGET).get() instanceof BlockPosLookTarget || mob.getBrain().isMemoryInState(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_PRESENT) && mob.getBrain().getOptionalMemory(MemoryModuleType.LOOK_TARGET).isPresent() && mob.getBrain().getOptionalMemory(MemoryModuleType.LOOK_TARGET).get() instanceof EntityLookTarget entityLookTarget && entityLookTarget.getEntity() instanceof LivingEntity living && !this.doesApply(living))) {
            mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            mob.getBrain().forget(MemoryModuleType.WALK_TARGET);
            mob.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(target, true));
        }
    }

    @Override
    protected Map<Activity, Pair<ImmutableList<? extends Task<?>>, ImmutableList<com.mojang.datafixers.util.Pair<MemoryModuleType<?>, MemoryModuleState>>>> tasksToApply() {
        return Map.of(ApoliActivities.FOLLOW, new Pair<>(ImmutableList.of(createFollowTask(this::doesApply, speed, distance, completionRange)), ImmutableList.of()));
    }

    public static SingleTickTask<LivingEntity> createFollowTask(Predicate<LivingEntity> predicate, float speed, float distance, int completionRange) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(MemoryModuleType.LOOK_TARGET)).apply(context, (walkTarget, lookTarget) -> (world, entity, time) -> {
            if (!(context.getValue(lookTarget) instanceof EntityLookTarget entityLookTarget)) {
                return false;
            }
            if (!(entityLookTarget.getEntity() instanceof LivingEntity living)) {
                return false;
            }
            if (!entity.isInRange(living, distance)) {
                return false;
            }
            if (!predicate.test(living)) {
                return false;
            }
            walkTarget.remember(new WalkTarget(context.getValue(lookTarget), speed, completionRange));
            return true;
        }));
    }

    public static BehaviorFactory<?> createFactory() {
        return new BehaviorFactory<>(Apoli.identifier("follow"),
                new SerializableData()
                        .add("priority", SerializableDataTypes.INT, 0)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("speed", SerializableDataTypes.FLOAT, 1.0F)
                        .add("distance", SerializableDataTypes.FLOAT)
                        .add("completion_range", SerializableDataTypes.INT, 0),
                (data, mob) -> new FollowMobBehavior(mob, data.getInt("priority"), data.get("bientity_condition"), data.getFloat("speed"), data.getFloat("distance"), data.getInt("completion_range")));
    }
}