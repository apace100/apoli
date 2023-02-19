package io.github.apace100.apoli.behavior.types;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.behavior.BehaviorFactory;
import io.github.apace100.apoli.behavior.MobBehavior;
import io.github.apace100.apoli.registry.ApoliActivities;
import io.github.apace100.apoli.registry.ApoliMemoryModuleTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class FleeMobBehavior extends MobBehavior {
    private final float fleeDistance;
    private final double speed;
    private final double fastSpeed;

    public FleeMobBehavior(int priority, float fleeDistance, double slowSpeed, double fastSpeed) {
        super(priority);
        this.fleeDistance = fleeDistance;
        this.speed = slowSpeed;
        this.fastSpeed = fastSpeed;
        biEntityPredicate = (pair) -> pair.getRight().getBoundingBox().intersects(pair.getLeft().getBoundingBox().expand(fleeDistance));
    }

    @Override
    protected void setToDataInstance(SerializableData.Instance dataInstance) {
        super.setToDataInstance(dataInstance);
        dataInstance.set("distance", this.fleeDistance);
        dataInstance.set("speed", this.speed);
        dataInstance.set("fast_speed", this.fastSpeed);
    }

    @Override
    public void initGoals(MobEntity mob) {
        if (!(mob instanceof PathAwareEntity) || usesBrain(mob)) return;
        Goal fleeGoal = new FleeEntityGoal<>((PathAwareEntity) mob, LivingEntity.class, fleeDistance, speed, fastSpeed, entity -> biEntityPredicate.test(new Pair<>(entity, mob)));
        this.addToGoalSelector(mob, fleeGoal);
    }

    @Override
    protected void updateMemories(MobEntity mob, LivingEntity powerHolder) {
        if (activeEntities.contains(mob) && mob.getBrain().hasMemoryModule(ApoliMemoryModuleTypes.AVOID_TARGET) && mob.getBrain().getOptionalMemory(ApoliMemoryModuleTypes.AVOID_TARGET).isPresent()) {
            if (mob.getBrain().getOptionalMemory(ApoliMemoryModuleTypes.AVOID_TARGET).get() != powerHolder) {
                activeEntities.remove(mob);
            }
            return;
        }
        if (!mob.getBrain().hasMemoryModule(ApoliMemoryModuleTypes.AVOID_TARGET)) {
            mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            mob.getBrain().forget(MemoryModuleType.WALK_TARGET);
            mob.getBrain().remember(ApoliMemoryModuleTypes.AVOID_TARGET, powerHolder, 20L);
        }
        activeEntities.add(mob);
    }

    private static boolean shouldForgetAvoidTask(LivingEntity entity, FleeMobBehavior behavior) {
        if (!(entity instanceof MobEntity mob)) {
            return true;
        }
        if (!behavior.isActive(mob)) {
            return false;
        }
        Brain<?> brain = mob.getBrain();
        if (!brain.hasMemoryModule(ApoliMemoryModuleTypes.AVOID_TARGET)) {
            return true;
        }
        if (brain.getOptionalMemory(ApoliMemoryModuleTypes.AVOID_TARGET).isPresent()) {
            LivingEntity livingEntity = brain.getOptionalMemory(ApoliMemoryModuleTypes.AVOID_TARGET).get();
            return !behavior.biEntityPredicate.test(new Pair<>(livingEntity, mob));
        }

        return true;
    }

    @Override
    public boolean isPassive(MobEntity mob, LivingEntity target) {
        return true;
    }

    private SingleTickTask<PathAwareEntity> createFleeTask(float slowSpeed, float fastSpeed, int range) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(ApoliMemoryModuleTypes.AVOID_TARGET)).apply(context, (walkTarget, posSource) -> (world, entity, time) -> {
            float speed = entity.squaredDistanceTo(context.getValue(posSource)) < 49.0 ? fastSpeed : slowSpeed;
            Vec3d vec3d2;
            Optional<WalkTarget> optional = context.getOptionalValue(walkTarget);
            if (optional.isPresent()) {
                return false;
            }
            Vec3d vec3d = entity.getPos();
            Function<Entity, Vec3d> posGetter = Entity::getPos;
            if (!vec3d.isInRange(vec3d2 = posGetter.apply(context.getValue(posSource)), range)) {
                return false;
            }
            for (int j = 0; j < 10; ++j) {
                Vec3d vec3d3;
                vec3d3 = NoPenaltyTargeting.findFrom(entity, 16, 7, vec3d2);
                if (vec3d3 == null) continue;
                walkTarget.remember(new WalkTarget(vec3d3, speed, 0));
                break;
            }
            return true;
        }));
    }

    @Override
    protected Map<Activity, Pair<ImmutableList<? extends Task<?>>, ImmutableList<com.mojang.datafixers.util.Pair<MemoryModuleType<?>, MemoryModuleState>>>> tasksToApply() {
        return Map.of(ApoliActivities.AVOID, new Pair<>(ImmutableList.of(createFleeTask((float)this.speed, (float)this.fastSpeed, (int)fleeDistance), ForgetTask.create((m) -> FleeMobBehavior.shouldForgetAvoidTask(m, this), ApoliMemoryModuleTypes.AVOID_TARGET)), ImmutableList.of(new com.mojang.datafixers.util.Pair<>(ApoliMemoryModuleTypes.AVOID_TARGET, MemoryModuleState.VALUE_PRESENT))));
    }

    public static BehaviorFactory<?> createFactory() {
        return new BehaviorFactory<>(Apoli.identifier("flee"),
                new SerializableData()
                        .add("priority", SerializableDataTypes.INT, 0)
                        .add("distance", SerializableDataTypes.FLOAT)
                        .add("speed", SerializableDataTypes.DOUBLE)
                        .addFunctionedDefault("fast_speed", SerializableDataTypes.DOUBLE, data -> data.getDouble("speed")),
                data -> new FleeMobBehavior(data.getInt("priority"), data.getFloat("distance"), data.getDouble("speed"), data.getDouble("fast_speed")));
    }
}