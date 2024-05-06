package io.github.apace100.apoli.power.factory.behavior.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.MobEntityAccessor;
import io.github.apace100.apoli.power.factory.behavior.MobBehaviorFactory;
import io.github.apace100.apoli.power.factory.behavior.MobBehavior;
import io.github.apace100.apoli.power.factory.behavior.goal.ConditionedLookAtEntityGoal;
import io.github.apace100.apoli.registry.ApoliActivities;
import io.github.apace100.apoli.registry.ApoliMemoryModuleTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class LookMobBehavior extends MobBehavior {
    private final Predicate<Pair<Entity, Entity>> moveBodyBientityCondition;
    private ConditionedLookAtEntityGoal goal;

    public LookMobBehavior(MobEntity mob, int priority, Predicate<Pair<Entity, Entity>> bientityCondition, Predicate<Pair<Entity, Entity>> moveBodyBientityCondition) {
        super(mob, priority, bientityCondition);
        this.moveBodyBientityCondition = moveBodyBientityCondition;
    }

    @Override
    public void applyGoals() {
        this.goal = new ConditionedLookAtEntityGoal(mob, this::doesApply);
        this.addToGoalSelector(goal);
    }

    public boolean shouldMoveBody() {
        return moveBodyBientityCondition != null && getLookTarget() != null && moveBodyBientityCondition.test(new Pair<>(mob, getLookTarget()));
    }

    @Nullable
    public Entity getLookTarget() {
        if (usesGoals()) {
            return goal.getTarget();
        } else if (usesBrain() && doesBrainHaveTarget()) {
            return ((EntityLookTarget)this.mob.getBrain().getOptionalMemory(MemoryModuleType.LOOK_TARGET).get()).getEntity();
        }
        return null;
    }

    public boolean doesBrainHaveTarget() {
        return this.mob.getBrain().getOptionalMemory(MemoryModuleType.LOOK_TARGET).isPresent() && this.mob.getBrain().getOptionalMemory(MemoryModuleType.LOOK_TARGET).get() instanceof EntityLookTarget lookTarget && this.doesApply(lookTarget.getEntity());
    }

    public void tickBody() {
        float yaw = ((MobEntityAccessor)this.mob).invokeChangeAngle(this.mob.getBodyYaw(), this.mob.headYaw, 90F);
        this.mob.setYaw(yaw);
        this.mob.setBodyYaw(yaw);
    }

    @Override
    protected Map<Activity, Pair<ImmutableList<? extends Task<?>>, List<MemoryModuleType<?>>>> tasksToApply() {
        return Map.of(ApoliActivities.LOOK, new Pair<>(ImmutableList.of(MobBehavior.taskWithBehaviorTargetTask(createRememberTask(this::doesApply), this::doesApply)), Lists.newArrayList()));
    }

    public static SingleTickTask<MobEntity> createRememberTask(Predicate<LivingEntity> predicate) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(ApoliMemoryModuleTypes.BEHAVIOR_TARGET), context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET)).apply(context, (behaviorTarget, lookTarget) -> (world, entity, time) -> {
            LivingEntity currentTarget = context.getValue(behaviorTarget);
            if ((context.getOptionalValue(lookTarget).isEmpty() || context.getOptionalValue(lookTarget).isPresent() && context.getOptionalValue(lookTarget).get() instanceof BlockPosLookTarget || context.getOptionalValue(lookTarget).isPresent() && context.getOptionalValue(lookTarget).get() instanceof EntityLookTarget entityLookTarget && entityLookTarget.getEntity() instanceof LivingEntity living && !predicate.test(living))) {
                entity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(currentTarget, true));
                return true;
            }
            return false;
        }));
    }

    public static MobBehaviorFactory<?> createFactory() {
        return new MobBehaviorFactory<>(Apoli.identifier("look"),
                new SerializableData()
                        .add("priority", SerializableDataTypes.INT, 0)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("move_body_bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
                (data, mob) -> new LookMobBehavior(mob, data.getInt("priority"), data.get("bientity_condition"), data.get("move_body_bientity_condition")));
    }
}