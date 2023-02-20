package io.github.apace100.apoli.behavior.types;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.behavior.BehaviorFactory;
import io.github.apace100.apoli.behavior.MobBehavior;
import io.github.apace100.apoli.behavior.goal.FollowGoal;
import io.github.apace100.apoli.registry.ApoliActivities;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.function.Predicate;

public class FollowMobBehavior extends MobBehavior {
    private final float speed;
    private final float distance;
    private final int completionRange;

    public FollowMobBehavior(int priority, float speed, float distance, int completionRange) {
        super(priority);
        this.speed = speed;
        this.distance = distance;
        this.completionRange = completionRange;
    }

    @Override
    public void initGoals(MobEntity mob) {
        if (!(mob instanceof PathAwareEntity pathAware) || usesBrain(mob)) return;
        this.addToGoalSelector(mob, new FollowGoal(pathAware, speed, distance, completionRange, biEntityPredicate));
    }

    @Override
    protected void tickMemories(MobEntity mob, LivingEntity other) {
        if (activeEntities.contains(mob) && mob.getBrain().hasMemoryModule(MemoryModuleType.LOOK_TARGET) && mob.getBrain().getOptionalMemory(MemoryModuleType.LOOK_TARGET).isPresent()) {
            if (mob.getBrain().getOptionalMemory(MemoryModuleType.LOOK_TARGET).get() != other) {
                activeEntities.remove(mob);
            }
        }
        if (biEntityPredicate.test(new Pair<>(other, mob)) && !activeEntities.contains(mob)) {
            mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            mob.getBrain().forget(MemoryModuleType.WALK_TARGET);
            mob.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(other, true));
            activeEntities.add(mob);
        }
    }

    @Override
    protected Map<Activity, Pair<ImmutableList<? extends Task<?>>, ImmutableList<com.mojang.datafixers.util.Pair<MemoryModuleType<?>, MemoryModuleState>>>> tasksToApply() {
        return Map.of(ApoliActivities.FOLLOW, new Pair<>(ImmutableList.of(createFollowTask((pair) -> biEntityPredicate.test(pair), speed, distance, completionRange)), ImmutableList.of()));
    }

    public static SingleTickTask<LivingEntity> createFollowTask(Predicate<Pair<LivingEntity, MobEntity>> predicate, float speed, float distance, int completionRange) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(MemoryModuleType.LOOK_TARGET)).apply(context, (walkTarget, lookTarget) -> (world, entity, time) -> {
            if (!(entity instanceof MobEntity mob)) {
                return false;
            }
            if (!(context.getValue(lookTarget) instanceof EntityLookTarget entityLookTarget)) {
                return false;
            }
            if (!(entityLookTarget.getEntity() instanceof LivingEntity living)) {
                return false;
            }
            if (!mob.isInRange(living, distance)) {
                return false;
            }
            if (!predicate.test(new Pair<>(living, mob))) {
                return false;
            }
            walkTarget.remember(new WalkTarget(context.getValue(lookTarget), speed, completionRange));
            return true;
        }));
    }

    @Override
    public boolean isHostile(MobEntity mob, LivingEntity target) {
        return true;
    }

    @Override
    protected void setToDataInstance(SerializableData.Instance dataInstance) {
        super.setToDataInstance(dataInstance);
        dataInstance.set("speed", speed);
        dataInstance.set("distance", distance);
        dataInstance.set("completion_range", completionRange);
    }

    public static BehaviorFactory<?> createFactory() {
        return new BehaviorFactory<>(Apoli.identifier("follow"),
                new SerializableData()
                        .add("priority", SerializableDataTypes.INT, 0)
                        .add("speed", SerializableDataTypes.FLOAT, 1.0F)
                        .add("distance", SerializableDataTypes.FLOAT)
                        .add("completion_range", SerializableDataTypes.INT, 0),
                data -> new FollowMobBehavior(data.getInt("priority"), data.getFloat("speed"), data.getFloat("distance"), data.getInt("completion_range")));
    }
}