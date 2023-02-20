package io.github.apace100.apoli.behavior.types;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.behavior.BehaviorFactory;
import io.github.apace100.apoli.behavior.MobBehavior;
import io.github.apace100.apoli.behavior.goal.TargetFollowGoal;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.AttributeContainerAccessor;
import io.github.apace100.apoli.mixin.DefaultAttributeContainerAccessor;
import io.github.apace100.apoli.registry.ApoliActivities;
import io.github.apace100.apoli.registry.ApoliMemoryModuleTypes;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.function.Predicate;

public class HostileMobBehavior extends MobBehavior {
    private final int attackCooldown;
    private final float speed;
    private final boolean attacks;
    private final List<AttributedEntityAttributeModifier> modifiers = new ArrayList<>();
    private final Set<EntityAttribute> modifiedAttributes = new HashSet<>();

    public HostileMobBehavior(int priority, int attackCooldown, float speed, boolean attacks) {
        super(priority);
        this.attackCooldown = attackCooldown;
        this.speed = speed;
        this.attacks = attacks;
    }

    @Override
    public void initGoals(MobEntity mob) {
        if (!(mob instanceof PathAwareEntity pathAware) || usesBrain(mob)) return;
        if (attacks) {
            this.addToGoalSelector(mob, new MeleeAttackGoal(pathAware, speed, false));
        } else {
            this.addToGoalSelector(mob, new TargetFollowGoal(pathAware, speed, false));
        }
        this.addToTargetSelector(mob, new ActiveTargetGoal<>(pathAware, LivingEntity.class, false, entity -> biEntityPredicate.test(new Pair<>(entity, mob))));
    }

    @Override
    protected void tickMemories(MobEntity mob, LivingEntity other) {
        if (activeEntities.contains(mob) && mob.getBrain().hasMemoryModule(ApoliMemoryModuleTypes.ATTACK_TARGET) && mob.getBrain().getOptionalMemory(ApoliMemoryModuleTypes.ATTACK_TARGET).isPresent()) {
            if (mob.getBrain().getOptionalMemory(ApoliMemoryModuleTypes.ATTACK_TARGET).get() != other) {
                activeEntities.remove(mob);
            }
        }
        if (!mob.getBrain().hasMemoryModule(ApoliMemoryModuleTypes.ATTACK_TARGET)) {
            mob.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            if (this.attacks) {
                mob.getBrain().remember(ApoliMemoryModuleTypes.ATTACK_TARGET, other, 200L);
                mob.setAttacking(true);
                mob.getBrain().doExclusively(ApoliActivities.FIGHT);
            }
            mob.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(other, true));
            activeEntities.add(mob);
        }
    }

    @Override
    public void onAttacked(MobEntity mob, Entity attacker) {
        if (MobBehavior.usesGoals(mob) || !(attacker instanceof LivingEntity livingAttacker) || !biEntityPredicate.test(new Pair<>(livingAttacker, mob))) return;
        Optional<LivingEntity> optional = mob.getBrain().getOptionalRegisteredMemory(ApoliMemoryModuleTypes.ATTACK_TARGET);
        LivingEntity livingEntity = LookTargetUtil.getCloserEntity(mob, optional, livingAttacker);

        mob.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
        if (this.attacks) {
            mob.getBrain().remember(ApoliMemoryModuleTypes.ATTACK_TARGET, livingAttacker, 200L);
            mob.setAttacking(true);
            mob.getBrain().doExclusively(ApoliActivities.FIGHT);
        }
        mob.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(livingEntity, true));
    }

    @Override
    public void onAdded(MobEntity mob) {
        Map<EntityAttribute, EntityAttributeInstance> modifierMap = new HashMap<>(((DefaultAttributeContainerAccessor)((AttributeContainerAccessor)mob.getAttributes()).getFallback()).getInstances());
        DefaultAttributeContainer.Builder builder = DefaultAttributeContainer.builder();

        modifierMap.forEach((attribute, instance) -> builder.add(attribute, instance.getBaseValue()));
        if (this.attacks && !modifierMap.containsKey(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
            builder.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.0);
            modifiedAttributes.add(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        }
        modifiers.forEach(modifier -> {
            if (modifierMap.containsKey(modifier.getAttribute()) || modifier.getAttribute() == EntityAttributes.GENERIC_ATTACK_DAMAGE) return;
            builder.add(modifier.getAttribute(), 0.0);
            modifiedAttributes.add(modifier.getAttribute());
        });
        ((AttributeContainerAccessor)mob.getAttributes()).setFallback(builder.build());

        modifiers.forEach(modifier -> mob.getAttributes().getCustomInstance(modifier.getAttribute()).addTemporaryModifier(modifier.getModifier()));
    }

    @Override
    public void onRemoved(MobEntity mob) {
        modifiers.forEach(modifier -> mob.getAttributes().getCustomInstance(modifier.getAttribute()).removeModifier(modifier.getModifier()));

        Map<EntityAttribute, EntityAttributeInstance> modifierMap = new HashMap<>(((DefaultAttributeContainerAccessor)((AttributeContainerAccessor)mob.getAttributes()).getFallback()).getInstances());
        DefaultAttributeContainer.Builder builder = DefaultAttributeContainer.builder();

        modifierMap.forEach((attribute, instance) -> {
            if (modifiedAttributes.contains(attribute) && instance.getModifiers().isEmpty()) return;
            builder.add(attribute, instance.getBaseValue());
        });
        ((AttributeContainerAccessor)mob.getAttributes()).setFallback(builder.build());
    }

    @Override
    protected Map<Activity, Pair<ImmutableList<? extends Task<?>>, ImmutableList<com.mojang.datafixers.util.Pair<MemoryModuleType<?>, MemoryModuleState>>>> tasksToApply() {
        return Map.of(ApoliActivities.FIGHT, new Pair<>(ImmutableList.of(createForgetTask(e -> MobBehavior.shouldForgetPowerHolder(e, this, ApoliMemoryModuleTypes.ATTACK_TARGET)), createAttackTask(this.attackCooldown, this.attacks), GoTowardsLookTargetTask.create(speed, 0)), ImmutableList.of(com.mojang.datafixers.util.Pair.of(ApoliMemoryModuleTypes.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT), com.mojang.datafixers.util.Pair.of(ApoliMemoryModuleTypes.ATTACK_COOLING_DOWN, MemoryModuleState.VALUE_PRESENT))));
    }

    public static SingleTickTask<MobEntity> createAttackTask(int cooldown, boolean attacks) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET), context.queryMemoryValue(ApoliMemoryModuleTypes.ATTACK_TARGET), context.queryMemoryAbsent(ApoliMemoryModuleTypes.ATTACK_COOLING_DOWN), context.queryMemoryValue(MemoryModuleType.VISIBLE_MOBS)).apply(context, (lookTarget, attackTarget, attackCoolingDown, visibleMobs) -> (world, entity, time) -> {
            LivingEntity livingEntity = context.getValue(attackTarget);
            if (entity.isInAttackRange(livingEntity) && context.getValue(visibleMobs).contains(livingEntity)) {
                lookTarget.remember(new EntityLookTarget(livingEntity, true));
                if (attacks) {
                    entity.swingHand(Hand.MAIN_HAND);
                    entity.tryAttack(livingEntity);
                    attackCoolingDown.remember(true, cooldown);
                }
                entity.setTarget(livingEntity);
                return true;
            }
            return false;
        }));
    }

    public static <E extends MobEntity> Task<E> createForgetTask(Predicate<LivingEntity> alternativeCondition) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(ApoliMemoryModuleTypes.ATTACK_TARGET), context.queryMemoryOptional(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply(context, (attackTarget, cantReachWalkTargetSince) -> (world, entity, time) -> {
            LivingEntity livingEntity = context.getValue(attackTarget);
            if (!entity.canTarget(livingEntity) || context.getOptionalValue(cantReachWalkTargetSince).isPresent() && livingEntity.world.getTime() - context.getOptionalValue(cantReachWalkTargetSince).get() > 200L || !livingEntity.isAlive() || livingEntity.world != entity.world || !alternativeCondition.test(livingEntity)) {
                attackTarget.forget();
                return true;
            }
            return false;
        }));
    }

    @Override
    public boolean isHostile(MobEntity mob, LivingEntity target) {
        return true;
    }

    @Override
    protected void setToDataInstance(SerializableData.Instance dataInstance) {
        super.setToDataInstance(dataInstance);
        dataInstance.set("attack_cooldown", attackCooldown);
        dataInstance.set("modifier", null);
        if (modifiers.size() > 0) {
            dataInstance.set("modifiers", modifiers);
        } else {
            dataInstance.set("modifiers", null);
        }
        dataInstance.set("speed", speed);
        dataInstance.set("attacks", attacks);
    }

    private void addModifier(AttributedEntityAttributeModifier modifier) {
        this.modifiers.add(modifier);
    }

    public static BehaviorFactory<?> createFactory() {
        return new BehaviorFactory<>(Apoli.identifier("hostile"),
                new SerializableData()
                        .add("priority", SerializableDataTypes.INT, 0)
                        .add("attack_cooldown", SerializableDataTypes.INT, 20)
                        .add("modifier", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIER, null)
                        .add("modifiers", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIERS, null)
                        .add("speed", SerializableDataTypes.FLOAT, 1.0F)
                        .add("attacks", SerializableDataTypes.BOOLEAN, false),
                data -> {
                    HostileMobBehavior behavior = new HostileMobBehavior(data.getInt("priority"), data.getInt("attack_cooldown"), data.getFloat("speed"), data.getBoolean("attacks"));
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