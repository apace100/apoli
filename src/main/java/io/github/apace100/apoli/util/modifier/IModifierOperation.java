package io.github.apace100.apoli.util.modifier;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.codec.StrictCodec;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.Entity;

import java.util.List;
import java.util.Locale;

public interface IModifierOperation {

    /**
     *  The strict data type for operations. Doesn't allow for
     */
    SerializableDataType<IModifierOperation> STRICT_DATA_TYPE = SerializableDataType.registry(ApoliRegistries.MODIFIER_OPERATION, Apoli.MODID, true);

    SerializableDataType<IModifierOperation> DATA_TYPE = SerializableDataType.of(
        new StrictCodec<>() {

            @Override
            public <T> Pair<IModifierOperation, T> strictDecode(DynamicOps<T> ops, T input) {

                DataResult<String> inputString = ops.getStringValue(input);
                if (inputString.isSuccess()) {

                    IModifierOperation operation = switch (inputString.getOrThrow().toLowerCase(Locale.ROOT)) {
                        case "addition", "add_value" ->
                            ModifierOperation.ADD_BASE_EARLY;
                        case "multiply_base", "add_multiplied_base" ->
                            ModifierOperation.MULTIPLY_BASE_ADDITIVE;
                        case "multiply_total", "add_multiplied_total" ->
                            ModifierOperation.MULTIPLY_TOTAL_MULTIPLICATIVE;
                        default ->
                            STRICT_DATA_TYPE.strictParse(ops, input);
                    };

                    return Pair.of(operation, input);

                }

                else {
                    return STRICT_DATA_TYPE.strictDecode(ops, input);
                }

            }

            @Override
            public <T> T strictEncode(IModifierOperation input, DynamicOps<T> ops, T prefix) {
                return STRICT_DATA_TYPE.strictEncode(input, ops, prefix);
            }

        },
        STRICT_DATA_TYPE.packetCodec()
    );

    /**
     *  @return the serializable data of the modifier instance that this operation needs to operate.
     */
    SerializableData getSerializableData();

    /**
     *  Specifies which value is modified by this modifier, which can either be the base value or the total value.
     *  All {@link Phase#BASE} modifiers are applied before {@link Phase#TOTAL} modifiers.
     *
     *  @return the {@link Phase} of this modifier.
     */
    Phase getPhase();

    /**
     *  The order of when this modifier is applied in relation to other modifiers with the same {@link Phase}. Higher
     *  values means the modifier will be applied later.
     *
     *  @return the order of this modifier.
     */
    int getOrder();

    /**
     *  Applies all instances of this modifier operation to the specified value.
     *
     *  @param entity       the entity these modifiers are on.
     *  @param instances    the serializable data instances of each modifier with this operation.
     *  @param base         the base value to operate on. With {@link Phase#BASE}, it refers to the original base value, while in
     *                      {@link Phase#TOTAL}, it's the <b>total</b> base value.
     *  @param current      the current value, which differs from the base value only if prior modifiers have modified it in the same phase.
     *  @return             the new current value after applying all modifier instances with this operation.
     */
    double apply(Entity entity, List<SerializableData.Instance> instances, double base, double current);

    enum Phase {
        BASE, TOTAL
    }

}
