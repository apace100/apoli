package io.github.apace100.apoli.behavior.types;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.behavior.BehaviorFactory;
import io.github.apace100.apoli.behavior.MobBehavior;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.AttributeContainerAccessor;
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
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HostileMobBehavior extends MobBehavior {
    private final int attackCooldown;
    private final float speed;

    private final List<AttributedEntityAttributeModifier> modifiers = new ArrayList<>();
    private final Set<EntityAttribute> modifiedAttributes = new HashSet<>();

    public HostileMobBehavior(MobEntity mob, int priority, Predicate<Pair<LivingEntity, LivingEntity>> bientityCondition, int attackCooldown, float speed) {
        super(mob, priority, bientityCondition);
        this.attackCooldown = attackCooldown;
        this.speed = speed;
    }

    @Override
    public void initGoals() {
        if (!usesGoals()) return;
        if (mob instanceof PathAwareEntity pathAware && (!(mob instanceof Angerable) || !(mob instanceof HostileEntity))) {
            this.addToGoalSelector(new MeleeAttackGoal(pathAware, speed, false));
        }
        this.addToTargetSelector(new ActiveTargetGoal<>(mob, LivingEntity.class, false, this::doesApply));
    }

    @Override
    protected void tickMemories(LivingEntity target) {
        if (mob.getBrain().hasMemoryModule(ApoliMemoryModuleTypes.ATTACK_TARGET) && mob.getBrain().hasActivity(Activity.FIGHT)) {
            mob.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            mob.getBrain().remember(MemoryModuleType.ATTACK_TARGET, target);
            mob.getBrain().doExclusively(ApoliActivities.FIGHT);
            mob.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(target, true));
        } else if (!mob.getBrain().hasMemoryModule(ApoliMemoryModuleTypes.ATTACK_TARGET)) {
            mob.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            mob.getBrain().remember(ApoliMemoryModuleTypes.ATTACK_TARGET, target, 200L);
            mob.setAttacking(true);
            mob.getBrain().doExclusively(ApoliActivities.FIGHT);
            mob.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(target, true));
        }
    }

    @Override
    public void onAttacked(Entity attacker) {
        if (!usesBrain() || !(attacker instanceof LivingEntity livingAttacker) || !doesApply(livingAttacker)) return;
        Optional<LivingEntity> optional = mob.getBrain().getOptionalRegisteredMemory(ApoliMemoryModuleTypes.ATTACK_TARGET);
        LivingEntity livingEntity = LookTargetUtil.getCloserEntity(mob, optional, livingAttacker);

        mob.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
        mob.getBrain().remember(ApoliMemoryModuleTypes.ATTACK_TARGET, livingAttacker, 200L);
        mob.setAttacking(true);
        mob.getBrain().doExclusively(ApoliActivities.FIGHT);
        mob.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(livingEntity, true));
    }

    @Override
    public void onAdded() {
        addModifiedAttributes(mob);
    }

    @Override
    public void onRemoved() {
        removeModifiedAttributes(mob);

        if (mob.getTarget() != null || mob instanceof Angerable angerable && angerable.getAngryAt() != null) {
            if (mob instanceof Angerable) {
                ((Angerable) mob).stopAnger();
            }
            mob.setTarget(null);
        }
    }

    private void addModifiedAttributes(MobEntity mob) {
        modifiers.forEach(modifier -> {
            if (mob.getAttributes().getCustomInstance(modifier.getAttribute()) == null) {
                ((AttributeContainerAccessor) mob.getAttributes()).getCustom().put(modifier.getAttribute(), new EntityAttributeInstance(modifier.getAttribute(), m -> {}));
            }
            mob.getAttributes().getCustomInstance(modifier.getAttribute()).addTemporaryModifier(modifier.getModifier());
        });
        modifiedAttributes.addAll(modifiers.stream().map(AttributedEntityAttributeModifier::getAttribute).collect(Collectors.toSet()));
        if (mob.getAttributes().getCustomInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) == null) {
            ((AttributeContainerAccessor) mob.getAttributes()).getCustom().put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE, m -> {}));
            modifiedAttributes.add(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        }
    }

    private void removeModifiedAttributes(MobEntity mob) {
        modifiers.forEach(modifier -> {
            if (mob.getAttributes().getCustomInstance(modifier.getAttribute()) == null) return;
            mob.getAttributes().getCustomInstance(modifier.getAttribute()).removeModifier(modifier.getModifier());
            if (modifiedAttributes.contains(modifier.getAttribute()) && mob.getAttributes().getCustomInstance(modifier.getAttribute()).getModifiers().isEmpty()) {
                ((AttributeContainerAccessor)mob.getAttributes()).getCustom().remove(modifier.getAttribute());
            }
        });
    }

    @Override
    protected Map<Activity, Pair<ImmutableList<? extends Task<?>>, ImmutableList<com.mojang.datafixers.util.Pair<MemoryModuleType<?>, MemoryModuleState>>>> tasksToApply() {
        if (mob.getBrain().hasActivity(Activity.FIGHT)) {
            return Map.of();
        }
        return Map.of(ApoliActivities.FIGHT, new Pair<>(ImmutableList.of(ForgetTask.create(e -> MobBehavior.shouldForgetTarget((MobEntity)e, this, ApoliMemoryModuleTypes.ATTACK_TARGET), ApoliMemoryModuleTypes.ATTACK_TARGET), createAttackTask(this.attackCooldown), createFollowTask(this::doesApply, speed)), ImmutableList.of(com.mojang.datafixers.util.Pair.of(ApoliMemoryModuleTypes.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT), com.mojang.datafixers.util.Pair.of(ApoliMemoryModuleTypes.ATTACK_COOLING_DOWN, MemoryModuleState.VALUE_PRESENT))));
    }

    public static SingleTickTask<MobEntity> createAttackTask(int cooldown) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET), context.queryMemoryValue(ApoliMemoryModuleTypes.ATTACK_TARGET), context.queryMemoryAbsent(ApoliMemoryModuleTypes.ATTACK_COOLING_DOWN), context.queryMemoryValue(MemoryModuleType.VISIBLE_MOBS)).apply(context, (lookTarget, attackTarget, attackCoolingDown, visibleMobs) -> (world, entity, time) -> {
            LivingEntity livingEntity = context.getValue(attackTarget);
            if (entity.isInAttackRange(livingEntity) && context.getValue(visibleMobs).contains(livingEntity)) {
                lookTarget.remember(new EntityLookTarget(livingEntity, true));
                entity.swingHand(Hand.MAIN_HAND);
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

    private void addModifier(AttributedEntityAttributeModifier modifier) {
        this.modifiers.add(modifier);
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