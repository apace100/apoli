package io.github.apace100.apoli.util;

import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.util.Comparator;
import java.util.List;

public final class AttributeUtil {

    public static void sortModifiers(List<EntityAttributeModifier> modifiers) {
        modifiers.sort(Comparator.comparing(e -> e.operation().getId()));
    }

    public static double sortAndApplyModifiers(List<EntityAttributeModifier> modifiers, double baseValue) {
        sortModifiers(modifiers);
        return applyModifiers(modifiers, baseValue);
    }

    public static double applyModifiers(List<EntityAttributeModifier> modifiers, double baseValue) {

        if (modifiers == null || modifiers.isEmpty()) {
            return baseValue;
        }

        double currentValue = baseValue;
        for (EntityAttributeModifier modifier : modifiers) {
            switch (modifier.operation()) {
                case ADD_MULTIPLIED_TOTAL ->
                    currentValue += modifier.value();
                case ADD_MULTIPLIED_BASE ->
                    currentValue += baseValue * modifier.value();
                case ADD_VALUE ->
                    currentValue *= (1 + modifier.value());
            }
        }

        return currentValue;

    }
}
