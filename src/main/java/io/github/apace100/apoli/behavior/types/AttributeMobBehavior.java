package io.github.apace100.apoli.behavior.types;

import com.google.common.collect.Comparators;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.behavior.MobBehavior;
import io.github.apace100.apoli.mixin.AttributeContainerAccessor;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.function.Predicate;

public class AttributeMobBehavior extends MobBehavior {
    private final Map<EntityAttribute, Set<EntityAttributeModifier>> modifiers = new HashMap<>();
    private final Set<EntityAttribute> modifiedAttributes = new HashSet<>();
    private static final Map<MobEntity, AttributeMobBehavior> PREVIOUS_HIGHEST_PRIORITY_BEHAVIOR = new HashMap<>();
    private static final HashMap<MobEntity, Set<AttributeMobBehavior>> ACTIVE_ATTRIBUTE_BEHAVIORS = new HashMap<>();

    public AttributeMobBehavior(MobEntity mob, int priority, Predicate<Pair<LivingEntity, LivingEntity>> bientityCondition) {
        super(mob, priority, bientityCondition);
    }

    @Override
    public void onAdded() {
        addActiveAttributeBehavior();
        Optional<AttributeMobBehavior> optional = ACTIVE_ATTRIBUTE_BEHAVIORS.get(mob).stream().max((o1, o2) -> Comparators.min(o1.getPriority(), o2.getPriority()));

        if (optional.isPresent() && optional.get() == this && !(PREVIOUS_HIGHEST_PRIORITY_BEHAVIOR.containsKey(mob) || PREVIOUS_HIGHEST_PRIORITY_BEHAVIOR.get(mob) != this)) {
            addModifiedAttributes();
            PREVIOUS_HIGHEST_PRIORITY_BEHAVIOR.put(mob, this);
        }
    }

    @Override
    public void onRemoved() {
        removeModifiedAttributes();
        removeActiveAttributeBehavior();
    }

    public boolean isActiveAttributeBehavior() {
        if (!ACTIVE_ATTRIBUTE_BEHAVIORS.containsKey(mob)) {
            return false;
        }
        Optional<AttributeMobBehavior> optional = ACTIVE_ATTRIBUTE_BEHAVIORS.get(mob).stream().max((o1, o2) -> Comparators.max(o1.getPriority(), o2.getPriority()));
        return optional.isPresent() && optional.get() == this;
    }

    public void addActiveAttributeBehavior() {
        ACTIVE_ATTRIBUTE_BEHAVIORS.compute(mob, (mob, set) -> {
            Set<AttributeMobBehavior> newSet = set == null ? new HashSet<>() : set;
            newSet.add(this);
            return newSet;
        });
    }

    public void removeActiveAttributeBehavior() {
        if (ACTIVE_ATTRIBUTE_BEHAVIORS.get(mob) == null || !ACTIVE_ATTRIBUTE_BEHAVIORS.get(mob).contains(this)) return;
        boolean wasHighestPriorityActive = this.isActiveAttributeBehavior();
        ACTIVE_ATTRIBUTE_BEHAVIORS.get(mob).remove(this);
        if (wasHighestPriorityActive) {
            Optional<AttributeMobBehavior> optional = ACTIVE_ATTRIBUTE_BEHAVIORS.get(mob).stream().max((o1, o2) -> Comparators.max(o1.getPriority(), o2.getPriority()));
            optional.ifPresent(behavior -> {
                behavior.addModifiedAttributes();
                PREVIOUS_HIGHEST_PRIORITY_BEHAVIOR.put(mob, behavior);
            });
        }
    }

    protected void addModifiedAttributes() {
        modifiers.forEach((attribute, modifiers) -> {
            if (mob.getAttributes().getCustomInstance(attribute) == null) {
                ((AttributeContainerAccessor) mob.getAttributes()).getCustom().put(attribute, new EntityAttributeInstance(attribute, m -> {}));
            }
            modifiers.forEach(modifier -> mob.getAttributes().getCustomInstance(attribute).addTemporaryModifier(modifier));
        });
        modifiedAttributes.addAll(modifiers.keySet());
        if (mob.getAttributes().getCustomInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) == null) {
            ((AttributeContainerAccessor) mob.getAttributes()).getCustom().put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE, m -> {}));
            modifiedAttributes.add(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        }
    }

    protected void removeModifiedAttributes() {
        modifiedAttributes.forEach(attribute -> {
            if (mob.getAttributes().getCustomInstance(attribute) == null) return;
            if (modifiers.containsKey(attribute)) {
                modifiers.get(attribute).forEach(modifier -> {
                    mob.getAttributes().getCustomInstance(attribute).removeModifier(modifier);
                });
            }
            if (mob.getAttributes().getCustomInstance(attribute).getModifiers().isEmpty()) {
                ((AttributeContainerAccessor)mob.getAttributes()).getCustom().remove(attribute);
            }
        });
        modifiedAttributes.clear();
    }

    public void addModifier(AttributedEntityAttributeModifier modifier) {
        this.modifiers.compute(modifier.getAttribute(), (attribute, set) -> {
            Set<EntityAttributeModifier> newSet = set == null ? new HashSet<>() : set;
            newSet.add(modifier.getModifier());
            return newSet;
        });
    }
}