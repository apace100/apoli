package io.github.apace100.apoli.util.modifier;

import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ModifierUtil {

    public static Modifier createSimpleModifier(IModifierOperation operation, double value) {
        SerializableData.Instance data = ModifierOperation.DATA.new Instance();
        data.set("value", value);
        data.set("resource", null);
        data.set("modifier", null);
        return new Modifier(operation, data);
    }

    public static Modifier fromAttributeModifier(EntityAttributeModifier attributeModifier) {
        IModifierOperation operation = null;
        switch(attributeModifier.getOperation()) {
            case ADDITION -> operation = ModifierOperation.ADD_BASE_EARLY;
            case MULTIPLY_BASE -> operation = ModifierOperation.MULTIPLY_BASE_ADDITIVE;
            case MULTIPLY_TOTAL -> operation = ModifierOperation.MULTIPLY_TOTAL_MULTIPLICATIVE;
        }
        if(operation == null) {
            throw new RuntimeException(
                "Could not construct generic modifier from attribute modifier. Unknown operation: "
                    + attributeModifier.getOperation());
        }
        return createSimpleModifier(operation, attributeModifier.getValue());
    }

    public static Map<IModifierOperation, List<SerializableData.Instance>> sortModifiers(List<Modifier> modifiers) {
        Map<IModifierOperation, List<SerializableData.Instance>> buckets = new HashMap<>();
        for(Modifier modifier : modifiers) {
            List<SerializableData.Instance> list = buckets.computeIfAbsent(modifier.getOperation(), op -> new LinkedList<>());
            list.add(modifier.getData());
        }
        return buckets;
    }

    public static double applyModifiers(Entity entity, List<Modifier> modifiers, double baseValue) {
        return applyModifiers(entity, sortModifiers(modifiers), baseValue);
    }

    public static double applyModifiers(Entity entity, Map<IModifierOperation, List<SerializableData.Instance>> modifiers, double baseValue) {
        double currentBase = baseValue;
        double currentValue = baseValue;
        List<IModifierOperation> operations = new LinkedList<>(modifiers.keySet());
        operations.sort(((o1, o2) -> {
            if(o1 == o2) {
                return 0;
            } else if(o1.getPhase() == o2.getPhase()) {
                return o1.getOrder() - o2.getOrder();
            } else {
                return o1.getPhase().ordinal() - o2.getPhase().ordinal();
            }
        }));
        IModifierOperation.Phase lastPhase = IModifierOperation.Phase.BASE;
        for(IModifierOperation op : operations) {
            List<SerializableData.Instance> data = modifiers.get(op);
            if(op.getPhase() != lastPhase) {
                currentBase = currentValue;
            }
            currentValue = op.apply(entity, data, currentBase, currentValue);
        }
        return currentValue;
    }
}
