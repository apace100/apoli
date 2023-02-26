package io.github.apace100.apoli.power.factory.behavior.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.behavior.MobBehaviorFactory;
import io.github.apace100.apoli.power.factory.behavior.MobBehavior;
import io.github.apace100.apoli.registry.ApoliActivities;
import io.github.apace100.apoli.registry.ApoliMemoryModuleTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.TypeFilter;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class LookMobBehavior extends MobBehavior {

    public LookMobBehavior(MobEntity mob, int priority, Predicate<Pair<LivingEntity, LivingEntity>> bientityCondition) {
        super(mob, priority, bientityCondition);
    }

    @Override
    public void tick() {
        if (!this.usesGoals()) return;
        LivingEntity livingEntity = mob.world.getClosestEntity((((ServerWorld)mob.world).getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), this::doesApply)), TargetPredicate.createNonAttackable(), mob, mob.getX(), mob.getY(), mob.getZ());
        if (livingEntity == null || !livingEntity.isAlive()) return;

        mob.getLookControl().lookAt(livingEntity);
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
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
                (data, mob) -> new LookMobBehavior(mob, data.getInt("priority"), data.get("bientity_condition")));
    }
}