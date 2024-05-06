package io.github.apace100.apoli.power.factory.behavior.types;

import com.google.common.collect.Comparators;
import io.github.apace100.apoli.power.factory.behavior.MobBehavior;
import io.github.apace100.apoli.mixin.AttributeContainerAccessor;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Pair;

import java.util.*;
import java.util.function.Predicate;

public class AttributeMobBehavior extends MobBehavior {
    private final Map<EntityAttribute, Set<EntityAttributeModifier>> modifiers = new HashMap<>();
    private final Set<EntityAttribute> modifiedAttributes = new HashSet<>();
    private final Set<EntityAttribute> requiredAttributes = new HashSet<>();
    private static final Map<MobEntity, AttributeMobBehavior> PREVIOUS_HIGHEST_PRIORITY_BEHAVIOR = new HashMap<>();
    private static final HashMap<MobEntity, Set<AttributeMobBehavior>> ACTIVE_ATTRIBUTE_BEHAVIORS = new HashMap<>();

    public AttributeMobBehavior(MobEntity mob, int priority, Predicate<Pair<Entity, Entity>> bientityCondition) {
        super(mob, priority, pair -> bientityCondition.test(pair) && isLiving(pair));
    }

    private static boolean isLiving(Pair<Entity, Entity> pair) {
        return pair.getLeft().isLiving() && pair.getRight().isLiving();
    }

    @Override
    public void onAdded() {
        addActiveAttributeBehavior();
        Optional<AttributeMobBehavior> optional = ACTIVE_ATTRIBUTE_BEHAVIORS.get(mob).stream().max((o1, o2) -> Comparators.min(o1.getPriority(), o2.getPriority()));

        if (optional.isPresent() && optional.get() == this && (!PREVIOUS_HIGHEST_PRIORITY_BEHAVIOR.containsKey(mob) || PREVIOUS_HIGHEST_PRIORITY_BEHAVIOR.get(mob) != this)) {
            addModifiedAttributes();
            PREVIOUS_HIGHEST_PRIORITY_BEHAVIOR.put(mob, this);
        }
    }

    @Override
    public void onRemoved() {
        removeActiveAttributeBehavior();
    }

    public boolean isActiveAttributeBehavior() {
        if (!ACTIVE_ATTRIBUTE_BEHAVIORS.containsKey(mob)) {
            return false;
        }
        Optional<AttributeMobBehavior> optional = ACTIVE_ATTRIBUTE_BEHAVIORS.get(mob).stream().max((o1, o2) -> Comparators.min(o1.getPriority(), o2.getPriority()));
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
            this.removeModifiedAttributes();
            Optional<AttributeMobBehavior> optional = ACTIVE_ATTRIBUTE_BEHAVIORS.get(mob).stream().max((o1, o2) -> Comparators.min(o1.getPriority(), o2.getPriority()));
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
        requiredAttributes.forEach((attribute) -> {
            if (mob.getAttributes().getCustomInstance(attribute) == null) {
                ((AttributeContainerAccessor) mob.getAttributes()).getCustom().put(attribute, new EntityAttributeInstance(attribute, m -> {}));
            }
        });
        requiredAttributes.addAll(modifiers.keySet());
    }

    protected void removeModifiedAttributes() {
        modifiedAttributes.forEach(attribute -> {
            if (mob.getAttributes().getCustomInstance(attribute) == null) return;
            if (modifiers.containsKey(attribute)) {
                modifiers.get(attribute).forEach(modifier -> {
                    mob.getAttributes().getCustomInstance(attribute).removeModifier(modifier.getId());
                });
            }
            if (mob.getAttributes().getCustomInstance(attribute).getModifiers().isEmpty()) {
                ((AttributeContainerAccessor)mob.getAttributes()).getCustom().remove(attribute);
            }
        });
        modifiedAttributes.clear();
    }

    public void addRequiredAttribute(EntityAttribute... attribute) {
        requiredAttributes.addAll(List.of(attribute));
    }

    public void addModifier(AttributedEntityAttributeModifier modifier) {
        this.modifiers.compute(modifier.getAttribute(), (attribute, set) -> {
            Set<EntityAttributeModifier> newSet = set == null ? new HashSet<>() : set;
            newSet.add(modifier.getModifier());
            return newSet;
        });
    }
}