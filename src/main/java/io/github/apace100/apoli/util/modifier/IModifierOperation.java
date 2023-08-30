package io.github.apace100.apoli.util.modifier;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.Entity;

import java.util.List;
import java.util.Locale;

public interface IModifierOperation {

    SerializableDataType<IModifierOperation> STRICT_DATA_TYPE =
        SerializableDataType.defaultedRegistry(IModifierOperation.class, ApoliRegistries.MODIFIER_OPERATION, Apoli.MODID);

    SerializableDataType<IModifierOperation> DATA_TYPE = new SerializableDataType<>(IModifierOperation.class,
        STRICT_DATA_TYPE::send, STRICT_DATA_TYPE::receive, (jsonElement -> {
        if(jsonElement.isJsonPrimitive()) {
            switch(jsonElement.getAsString().toLowerCase(Locale.ROOT)) {
                case "addition":
                    return ModifierOperation.ADD_BASE_EARLY;
                case "multiply_base":
                    return ModifierOperation.MULTIPLY_BASE_ADDITIVE;
                case "multiply_total":
                    return ModifierOperation.MULTIPLY_TOTAL_MULTIPLICATIVE;
            }
        }
        return STRICT_DATA_TYPE.read(jsonElement);
    }));

    /**
     * Specifies which value is modified by this modifier, either
     * the base value or the total. All Phase.BASE modifiers will run before
     * the first Phase.TOTAL modifier.
     * @return
     */
    Phase getPhase();

    /**
     * Specifies when this modifier is applied, related to other modifiers in the same phase.
     * Higher number means it applies later.
     * @return order value
     */
    int getOrder();

    /**
     * The data that a modifier instance with this operation needs to operate.
     */
    SerializableData getData();

    /**
     * Applies all instances of this modifier operation to the value.
     * @param entity The entity these modifiers are on
     * @param instances The individual data instance of each modifier of this type
     * @param base The base value, in Phase.BASE this is the original base, in Phase.TOTAL it's the total base value
     * @param current The current value, which differs from the base value only if prior modifiers modified it in the same phase
     * @return The new current value after applying all instances of this modifier operation
     */
    double apply(Entity entity, List<SerializableData.Instance> instances, double base, double current);

    enum Phase {
        BASE, TOTAL
    }
}
