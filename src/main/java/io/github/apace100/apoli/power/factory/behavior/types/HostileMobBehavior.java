package io.github.apace100.apoli.power.factory.behavior.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.behavior.BehaviorFactory;
import io.github.apace100.apoli.power.factory.behavior.MobBehavior;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliActivities;
import io.github.apace100.apoli.registry.ApoliMemoryModuleTypes;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.function.Predicate;

public class HostileMobBehavior extends AttributeMobBehavior {
    private final int attackCooldown;
    private final float speed;

    public HostileMobBehavior(MobEntity mob, int priority, Predicate<Pair<LivingEntity, LivingEntity>> bientityCondition, int attackCooldown, float speed) {
        super(mob, priority, bientityCondition);
        this.attackCooldown = attackCooldown;
        this.speed = speed;
        addRequiredAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE);
    }

    @Override
    public void initGoals() {
        if (!usesGoals()) return;
        if (mob instanceof PathAwareEntity pathAware && (!(mob instanceof Angerable) || !(mob instanceof HostileEntity))) {
            this.addToGoalSelector(new MeleeAttackGoal(pathAware, speed, false));
        }
        this.addToTargetSelector(new ActiveTargetGoal<>(mob, LivingEntity.class, false, living -> this.doesApply(living) && mob.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE)));
    }

    @Override
    protected Map<Activity, Pair<ImmutableList<? extends Task<?>>, ImmutableList<com.mojang.datafixers.util.Pair<MemoryModuleType<?>, MemoryModuleState>>>> tasksToApply() {
        if (mob.getBrain().isMemoryInState(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT) || mob.getBrain().isMemoryInState(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_ABSENT)) {
            return Maps.newHashMap();
        }
        return Map.of(ApoliActivities.FIGHT, new Pair<>(ImmutableList.of(ForgetTask.create(e -> MobBehavior.shouldForgetTarget((MobEntity)e, this, ApoliMemoryModuleTypes.ATTACK_TARGET), ApoliMemoryModuleTypes.ATTACK_TARGET), createAttackTask(this.attackCooldown), createFollowTask(this::doesApply, speed)), ImmutableList.of(com.mojang.datafixers.util.Pair.of(ApoliMemoryModuleTypes.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT), com.mojang.datafixers.util.Pair.of(ApoliMemoryModuleTypes.ATTACK_COOLING_DOWN, MemoryModuleState.VALUE_PRESENT))));
    }

    @Override
    protected void tickMemories(LivingEntity target) {
        if (!mob.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE) || !Sensor.testAttackableTargetPredicateIgnoreVisibility(mob, target)) return;
        if (!mob.getBrain().hasActivity(ApoliActivities.FIGHT) && mob.getBrain().isMemoryInState(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_ABSENT) && (mob.getBrain().isMemoryInState(MemoryModuleType.ANGRY_AT, MemoryModuleState.VALUE_ABSENT) || mob.getBrain().isMemoryInState(MemoryModuleType.ANGRY_AT, MemoryModuleState.VALUE_PRESENT) && LookTargetUtil.getEntity(mob, MemoryModuleType.ANGRY_AT).isPresent() && !doesApply(LookTargetUtil.getEntity(mob, MemoryModuleType.ANGRY_AT).get()))) {
            mob.getBrain().remember(MemoryModuleType.ANGRY_AT, target.getUuid(), 600L);
        } else if (!mob.getBrain().hasActivity(ApoliActivities.FIGHT) && (mob.getBrain().isMemoryInState(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_ABSENT) || mob.getBrain().isMemoryInState(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT) && mob.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).isPresent() && !doesApply(mob.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).get()))) {
            mob.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            mob.getBrain().remember(MemoryModuleType.ATTACK_TARGET, target, 600L);
        } else if (mob.getBrain().hasActivity(ApoliActivities.FIGHT) && (mob.getBrain().isMemoryInState(ApoliMemoryModuleTypes.ATTACK_TARGET, MemoryModuleState.VALUE_ABSENT) || mob.getBrain().isMemoryInState(ApoliMemoryModuleTypes.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT) && mob.getBrain().getOptionalMemory(ApoliMemoryModuleTypes.ATTACK_TARGET).isPresent() && !doesApply(mob.getBrain().getOptionalMemory(ApoliMemoryModuleTypes.ATTACK_TARGET).get()))) {
            mob.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            mob.getBrain().remember(ApoliMemoryModuleTypes.ATTACK_TARGET, target, 600L);
        }
    }

    @Override
    public void onAttacked(Entity attacker) {
        if (!usesBrain() || !mob.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE) || !(attacker instanceof LivingEntity livingAttacker) || !Sensor.testAttackableTargetPredicateIgnoreVisibility(mob, livingAttacker) || !mob.getBrain().hasActivity(ApoliActivities.FIGHT)) return;
        Optional<LivingEntity> optional = mob.getBrain().getOptionalRegisteredMemory(ApoliMemoryModuleTypes.ATTACK_TARGET);
        LivingEntity livingEntity = LookTargetUtil.getCloserEntity(mob, optional, livingAttacker);

        mob.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
        mob.getBrain().remember(ApoliMemoryModuleTypes.ATTACK_TARGET, livingAttacker, 600L);
        mob.getBrain().doExclusively(ApoliActivities.FIGHT);
        mob.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(livingEntity, true));
    }

    public static SingleTickTask<MobEntity> createAttackTask(int cooldown) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET), context.queryMemoryValue(ApoliMemoryModuleTypes.ATTACK_TARGET), context.queryMemoryAbsent(ApoliMemoryModuleTypes.ATTACK_COOLING_DOWN), context.queryMemoryValue(MemoryModuleType.VISIBLE_MOBS)).apply(context, (lookTarget, attackTarget, attackCoolingDown, visibleMobs) -> (world, entity, time) -> {
            LivingEntity livingEntity = context.getValue(attackTarget);
            if (!entity.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
                return false;
            }
            if (entity.isInAttackRange(livingEntity) && context.getValue(visibleMobs).contains(livingEntity)) {
                lookTarget.remember(new EntityLookTarget(livingEntity, true));
                entity.swingHand(Hand.MAIN_HAND);
                entity.setAttacking(true);
                entity.tryAttack(livingEntity);
                attackCoolingDown.remember(true, cooldown);
                entity.setTarget(livingEntity);
                return true;
            }
            return false;
        }));
    }

    public static SingleTickTask<LivingEntity> createFollowTask(Predicate<LivingEntity> predicate, float speed) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(ApoliMemoryModuleTypes.ATTACK_TARGET)).apply(context, (walkTarget, lookTarget) -> (world, entity, time) -> {
            if (!predicate.test(context.getValue(lookTarget))) {
                return false;
            }
            walkTarget.remember(new WalkTarget(context.getValue(lookTarget), speed, (int)Math.sqrt(entity.getWidth() * 2.0f * (entity.getWidth() * 2.0f) + context.getValue(lookTarget).getWidth())));
            return true;
        }));
    }

    public static BehaviorFactory<?> createFactory() {
        return new BehaviorFactory<>(Apoli.identifier("hostile"),
                new SerializableData()
                        .add("priority", SerializableDataTypes.INT, 0)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("attack_cooldown", SerializableDataTypes.INT, 20)
                        .add("modifier", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIER, null)
                        .add("modifiers", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIERS, null)
                        .add("speed", SerializableDataTypes.FLOAT, 1.0F),
                (data, mob) -> {
                    HostileMobBehavior behavior = new HostileMobBehavior(mob, data.getInt("priority"), data.get("bientity_condition"), data.getInt("attack_cooldown"), data.getFloat("speed"));
                    if (data.isPresent("modifier")) {
                        behavior.addModifier(data.get("modifier"));
                    }
                    if (data.isPresent("modifiers")) {
                        ((List<AttributedEntityAttributeModifier>)data.get("modifiers")).forEach(behavior::addModifier);
                    }
                    return behavior;
                });
    }
}