package io.github.apace100.apoli.power.factory.behavior.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.behavior.MobBehaviorFactory;
import io.github.apace100.apoli.power.factory.behavior.MobBehavior;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.behavior.goal.ConditionedRevengeGoal;
import io.github.apace100.apoli.registry.ApoliActivities;
import io.github.apace100.apoli.registry.ApoliMemoryModuleTypes;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypeFilter;

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
    public void applyGoals() {
        if (!usesGoals()) return;
        if (mob instanceof PathAwareEntity pathAware) {
            this.addToTargetSelector(-1, new ConditionedRevengeGoal(pathAware, this::isHostile));
            if ((!(mob instanceof Angerable) || !(mob instanceof HostileEntity))) {
                this.addToGoalSelector(new MeleeAttackGoal(pathAware, speed, false));
            }
        }
        this.addToTargetSelector(new ActiveTargetGoal<>(mob, LivingEntity.class, false, living -> this.isHostile(living) && mob.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE)));
    }

    @Override
    protected Map<Activity, Pair<ImmutableList<? extends Task<?>>, List<MemoryModuleType<?>>>> tasksToApply() {
        if (hasMemoryModule(MemoryModuleType.ATTACK_TARGET)) {
            return Maps.newHashMap();
        }
        return Map.of(ApoliActivities.FIGHT, new Pair<>(ImmutableList.of(ForgetTask.create(e -> MobBehavior.shouldForgetTarget((MobEntity)e, this, ApoliMemoryModuleTypes.ATTACK_TARGET), ApoliMemoryModuleTypes.ATTACK_TARGET), MobBehavior.taskWithBehaviorTargetTask(createRememberTask(this::isHostile), this::isHostile), createAttackTask(this.attackCooldown), createFollowTask(this::doesApply, speed)), Lists.newArrayList(ApoliMemoryModuleTypes.ATTACK_TARGET, ApoliMemoryModuleTypes.ATTACK_COOLING_DOWN)));
    }

    @Override
    public void tick() {
        if (this.usesBrain() && (hasMemoryModule(MemoryModuleType.ANGRY_AT) || hasMemoryModule(MemoryModuleType.ATTACK_TARGET))) {
            List<? extends LivingEntity> applicableEntities = (((ServerWorld)mob.world).getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), this::doesApply));
            LivingEntity closestTarget = mob.world.getClosestEntity(applicableEntities, TargetPredicate.createNonAttackable(), mob, mob.getX(), mob.getY(), mob.getZ());

            if (closestTarget == null) return;

            if (hasMemoryModule(MemoryModuleType.ANGRY_AT)) {
                Optional<LivingEntity> optional = LookTargetUtil.getEntity(mob, MemoryModuleType.ANGRY_AT);
                if ((optional.isEmpty() || !doesApply(optional.get()))) {
                    mob.getBrain().remember(MemoryModuleType.ANGRY_AT, closestTarget.getUuid(), 600L);
                }
            } else if (hasMemoryModule(MemoryModuleType.ATTACK_TARGET)) {
                Optional<LivingEntity> optional = mob.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET);
                if ( (optional.isEmpty() || !doesApply(optional.get()))) {
                    mob.getBrain().remember(MemoryModuleType.ATTACK_TARGET, closestTarget, 600L);
                }
            }
        }
    }

    @Override
    public void onAttacked(Entity attacker) {
        if (!usesBrain() || !mob.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE) || !(attacker instanceof LivingEntity livingAttacker) || !Sensor.testAttackableTargetPredicateIgnoreVisibility(mob, livingAttacker) || !mob.getBrain().hasActivity(ApoliActivities.FIGHT) || !doesApply(livingAttacker)) return;

        mob.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        if (mob.getBrain().hasMemoryModule(ApoliMemoryModuleTypes.ATTACK_TARGET)) {
            Optional<LivingEntity> optional = mob.getBrain().getOptionalRegisteredMemory(ApoliMemoryModuleTypes.ATTACK_TARGET);
            LivingEntity livingEntity = optional.isEmpty() ? livingAttacker : LookTargetUtil.getCloserEntity(mob, optional, livingAttacker);
            mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            mob.getBrain().remember(ApoliMemoryModuleTypes.ATTACK_TARGET, livingEntity, 600L);
        } else if (hasMemoryModule(MemoryModuleType.ATTACK_TARGET)) {
            Optional<LivingEntity> optional = mob.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET);
            LivingEntity livingEntity = optional.isEmpty() ? livingAttacker : LookTargetUtil.getCloserEntity(mob, optional, livingAttacker);
            mob.getBrain().remember(MemoryModuleType.ATTACK_TARGET, livingEntity, 600L);
        }
    }

    public boolean isHostile(LivingEntity target) {
        return super.doesApply(target);
    }

    public static SingleTickTask<MobEntity> createRememberTask(Predicate<LivingEntity> predicate) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(ApoliMemoryModuleTypes.BEHAVIOR_TARGET), context.queryMemoryOptional(ApoliMemoryModuleTypes.ATTACK_TARGET), context.queryMemoryOptional(MemoryModuleType.ANGRY_AT), context.queryMemoryOptional(MemoryModuleType.ATTACK_TARGET)).apply(context, (behaviorTarget, apoliTarget, angryAt, attackTarget) -> (world, entity, time) -> {
            LivingEntity currentTarget = context.getValue(behaviorTarget);
            if (!entity.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE) || !Sensor.testAttackableTargetPredicateIgnoreVisibility(entity, currentTarget)) {
                return false;
            }

            if (!entity.getBrain().hasActivity(ApoliActivities.FIGHT) && context.getOptionalValue(attackTarget).isEmpty() && (context.getOptionalValue(angryAt).isEmpty() || context.getOptionalValue(angryAt).isPresent() && LookTargetUtil.getEntity(entity, MemoryModuleType.ANGRY_AT).isPresent() && !predicate.test(LookTargetUtil.getEntity(entity, MemoryModuleType.ANGRY_AT).get()))) {
                angryAt.remember(currentTarget.getUuid(), 600L);
                return true;
            } else if (!entity.getBrain().hasActivity(ApoliActivities.FIGHT) && (context.getOptionalValue(attackTarget).isEmpty() || context.getOptionalValue(attackTarget).isPresent() && !predicate.test(context.getOptionalValue(attackTarget).get()))) {
                entity.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
                attackTarget.remember(currentTarget, 600L);
                return true;
            } else if (entity.getBrain().hasActivity(ApoliActivities.FIGHT) && (context.getOptionalValue(apoliTarget).isEmpty() || context.getOptionalValue(apoliTarget).isPresent() && !predicate.test(context.getOptionalValue(apoliTarget).get()))) {
                entity.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
                entity.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
                apoliTarget.remember(currentTarget, 600L);
                return true;
            }
            return false;
        }));
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

    public static MobBehaviorFactory<?> createFactory() {
        return new MobBehaviorFactory<>(Apoli.identifier("hostile"),
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