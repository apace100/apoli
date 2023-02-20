package io.github.apace100.apoli.behavior.types;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.behavior.BehaviorFactory;
import io.github.apace100.apoli.behavior.MobBehavior;
import io.github.apace100.apoli.behavior.goal.FollowGoal;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.AttributeContainerAccessor;
import io.github.apace100.apoli.mixin.DefaultAttributeContainerAccessor;
import io.github.apace100.apoli.registry.ApoliActivities;
import io.github.apace100.apoli.registry.ApoliMemoryModuleTypes;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.function.Predicate;

public class FollowMobBehavior extends MobBehavior {
    private final float speed;
    private final int completionRange;

    public FollowMobBehavior(int priority, float speed, int completionRange) {
        super(priority);
        this.speed = speed;
        this.completionRange = completionRange;
    }

    @Override
    public void initGoals(MobEntity mob) {
        if (!(mob instanceof PathAwareEntity pathAware) || usesBrain(mob)) return;
        this.addToGoalSelector(mob, new FollowGoal(pathAware, speed, completionRange, biEntityPredicate));
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
        return Map.of(ApoliActivities.FOLLOW, new Pair<>(ImmutableList.of(GoTowardsLookTargetTask.create(speed, completionRange)), ImmutableList.of()));
    }

    @Override
    public boolean isHostile(MobEntity mob, LivingEntity target) {
        return true;
    }

    @Override
    protected void setToDataInstance(SerializableData.Instance dataInstance) {
        super.setToDataInstance(dataInstance);
        dataInstance.set("speed", speed);
        dataInstance.set("completion_range", completionRange);
    }

    public static BehaviorFactory<?> createFactory() {
        return new BehaviorFactory<>(Apoli.identifier("follow"),
                new SerializableData()
                        .add("priority", SerializableDataTypes.INT, 0)
                        .add("speed", SerializableDataTypes.FLOAT, 1.0F)
                        .add("completion_range", SerializableDataTypes.INT, 0),
                data -> new FollowMobBehavior(data.getInt("priority"), data.getFloat("speed"), data.getInt("completion_range")));
    }
}