package io.github.apace100.apoli.util.modifier;

import com.google.common.util.concurrent.AtomicDouble;
import io.github.apace100.calio.data.SerializableData;
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
        return applyModifiers(entity, sortModifiers(modifiers), baseValue);
    }

    private static Map<IModifierOperation, List<SerializableData.Instance>> sortModifiers(Collection<Modifier> modifiers) {

        Map<IModifierOperation, List<SerializableData.Instance>> buckets = new LinkedHashMap<>();
        modifiers.forEach(mod -> buckets
            .computeIfAbsent(mod.getOperation(), op -> new LinkedList<>())
            .add(mod.getData()));

        return buckets;

    }

    private static double applyModifiers(Entity entity, Map<IModifierOperation, List<SerializableData.Instance>> modifiers, double baseValue) {

        final AtomicDouble currentBase = new AtomicDouble(baseValue);
        final AtomicDouble currentValue = new AtomicDouble(baseValue);

        modifiers.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey(IModifierOperation.COMPARATOR))
            .forEach(opAndData -> {

                IModifierOperation operation = opAndData.getKey();
                List<SerializableData.Instance> dataList = opAndData.getValue();

                if (operation.getPhase() != IModifierOperation.Phase.BASE) {
                    currentBase.set(currentValue.get());
                }

                currentValue.set(operation.apply(entity, dataList, currentBase.get(), currentValue.get()));

            });

        return currentValue.get();

    }

}
