package io.github.apace100.apoli.util.modifier;

import io.github.apace100.calio.data.SerializableData;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.util.*;

public class ModifierUtil {

    /**
     *  Use {@link Modifier#of(ModifierOperation, double)} instead.
     */
    @Deprecated(forRemoval = true)
    public static Modifier createSimpleModifier(ModifierOperation operation, double amount) {
        return Modifier.of(operation, amount);
    }

    public static Modifier fromAttributeModifier(EntityAttributeModifier attributeModifier) {

        ModifierOperation operation = switch (attributeModifier.operation()) {
            case ADD_VALUE ->
                ModifierOperation.ADD_BASE_EARLY;
            case ADD_MULTIPLIED_BASE ->
                ModifierOperation.MULTIPLY_BASE_MULTIPLICATIVE;
            case ADD_MULTIPLIED_TOTAL ->
                ModifierOperation.MULTIPLY_TOTAL_MULTIPLICATIVE;
        };

        return Modifier.of(operation, attributeModifier.value());

    }

    public static double applyModifiers(Entity entity, Collection<Modifier> modifiers, double baseValue) {

        if (modifiers.isEmpty()) {
            return baseValue;
        }

        else {
            return applyModifiers(entity, sortModifiers(modifiers), baseValue);
        }

    }

    private static Map<IModifierOperation, List<SerializableData.Instance>> sortModifiers(Collection<Modifier> modifiers) {

        Map<IModifierOperation, List<SerializableData.Instance>> buckets = new Object2ObjectLinkedOpenHashMap<>();
        modifiers.stream()
            .sorted(Modifier::compareTo)
            .forEach(mod -> buckets
                .computeIfAbsent(mod.getOperation(), op -> new ObjectArrayList<>())
                .add(mod.getData()));

        return buckets;

    }

    private static double applyModifiers(Entity entity, Map<IModifierOperation, List<SerializableData.Instance>> operations, double baseValue) {

        double currentBase = baseValue;
        double currentValue = baseValue;

        IModifierOperation.Phase prevPhase = IModifierOperation.Phase.BASE;
        for (var operationEntry : operations.entrySet()) {

            IModifierOperation operation = operationEntry.getKey();
            IModifierOperation.Phase currPhase = operation.getPhase();

            if (currPhase != prevPhase) {
                prevPhase = currPhase;
                currentBase = currentValue;
            }

            currentValue = operation.apply(entity, operationEntry.getValue(), currentBase, currentValue);

        }

        return currentValue;

    }

}
