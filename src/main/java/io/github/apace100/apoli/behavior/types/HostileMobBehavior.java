package io.github.apace100.apoli.behavior.types;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.behavior.BehaviorFactory;
import io.github.apace100.apoli.behavior.MobBehavior;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.AttributeContainerAccessor;
import io.github.apace100.apoli.mixin.DefaultAttributeContainerAccessor;
import io.github.apace100.apoli.mixin.DefaultAttributeContainerBuilderAccessor;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;

import java.util.*;

public class HostileMobBehavior extends MobBehavior {
    private final List<AttributedEntityAttributeModifier> modifiers = new ArrayList<>();
    private final Set<EntityAttribute> modifiedAttributes = new HashSet<>();

    public HostileMobBehavior(int priority) {
        super(priority);
    }

    @Override
    public void initGoals(MobEntity mob) {
        if (!(mob instanceof PathAwareEntity pathAware) || usesBrain(mob)) return;
        this.addToGoalSelector(mob, new MeleeAttackGoal(pathAware, 1.0, false));
        this.addToTargetSelector(mob, new ActiveTargetGoal<>(pathAware, LivingEntity.class, false, entity -> biEntityPredicate.test(new net.minecraft.util.Pair<>(entity, mob))));
    }

    @Override
    public void onAdded(MobEntity mob) {
        Map<EntityAttribute, EntityAttributeInstance> modifierMap = new HashMap<>(((DefaultAttributeContainerAccessor)((AttributeContainerAccessor)mob.getAttributes()).getFallback()).getInstances());
        DefaultAttributeContainer.Builder builder = DefaultAttributeContainer.builder();

        modifierMap.forEach((attribute, instance) -> builder.add(attribute, instance.getBaseValue()));
        if (!modifierMap.containsKey(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
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

        modifierMap.forEach((attribute, instance) -> builder.add(attribute, instance.getBaseValue()));
        modifiedAttributes.forEach(modifier -> ((DefaultAttributeContainerBuilderAccessor)builder).getInstances().remove(modifier));
        modifierMap.forEach((attribute, instance) -> {
            if (!modifiedAttributes.contains(attribute)) return;
            ((AttributeContainerAccessor)mob.getAttributes()).getTracked().remove(instance);
        });
        ((AttributeContainerAccessor)mob.getAttributes()).setFallback(builder.build());
    }

    // TODO: Handle tasks
    @Override
    protected Map<Activity, net.minecraft.util.Pair<ImmutableList<? extends Task<?>>, ImmutableList<Pair<MemoryModuleType<?>, MemoryModuleState>>>> tasksToApply() {
        return Map.of();
    }

    @Override
    public boolean isHostile(MobEntity mob, LivingEntity target) {
        return true;
    }

    @Override
    protected void setToDataInstance(SerializableData.Instance dataInstance) {
        super.setToDataInstance(dataInstance);
        dataInstance.set("modifier", null);
        if (modifiers.size() > 0) {
            dataInstance.set("modifiers", modifiers);
        } else {
            dataInstance.set("modifiers", null);
        }
    }

    private void addModifier(AttributedEntityAttributeModifier modifier) {
        this.modifiers.add(modifier);
    }

    public static BehaviorFactory<?> createFactory() {
        return new BehaviorFactory<>(Apoli.identifier("hostile"),
                new SerializableData()
                        .add("priority", SerializableDataTypes.INT, 0)
                        .add("modifier", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIER, null)
                        .add("modifiers", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIERS, null),
                data -> {
                    HostileMobBehavior behavior = new HostileMobBehavior(data.getInt("priority"));
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