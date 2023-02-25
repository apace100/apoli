package io.github.apace100.apoli.power.factory.behavior.types;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.behavior.BehaviorFactory;
import io.github.apace100.apoli.power.factory.behavior.MobBehavior;
import io.github.apace100.apoli.power.factory.behavior.goal.LookAtTargetGoal;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class LookMobBehavior extends MobBehavior {

    public LookMobBehavior(MobEntity mob, int priority, Predicate<Pair<LivingEntity, LivingEntity>> bientityCondition) {
        super(mob, priority, bientityCondition);
    }

    @Override
    public void initGoals() {
        if (!(mob instanceof PathAwareEntity pathAware) || !usesGoals()) return;
        this.addToGoalSelector(new LookAtTargetGoal(pathAware, this::doesApply));
    }

    @Override
    protected void tickMemories(LivingEntity target) {
        if ((mob.getBrain().isMemoryInState(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT) || mob.getBrain().isMemoryInState(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_PRESENT) && mob.getBrain().getOptionalMemory(MemoryModuleType.LOOK_TARGET).isPresent() && mob.getBrain().getOptionalMemory(MemoryModuleType.LOOK_TARGET).get() instanceof BlockPosLookTarget || mob.getBrain().isMemoryInState(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_PRESENT) && mob.getBrain().getOptionalMemory(MemoryModuleType.LOOK_TARGET).isPresent() && mob.getBrain().getOptionalMemory(MemoryModuleType.LOOK_TARGET).get() instanceof EntityLookTarget entityLookTarget && entityLookTarget.getEntity() instanceof LivingEntity living && !this.doesApply(living))) {
            mob.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(target, true));
        }
    }

    public static BehaviorFactory<?> createFactory() {
        return new BehaviorFactory<>(Apoli.identifier("look"),
                new SerializableData()
                        .add("priority", SerializableDataTypes.INT, 0)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
                (data, mob) -> new LookMobBehavior(mob, data.getInt("priority"), data.get("bientity_condition")));
    }
}