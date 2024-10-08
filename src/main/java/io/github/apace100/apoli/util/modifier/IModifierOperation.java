package io.github.apace100.apoli.util.modifier;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.Entity;

import java.util.Collection;

public interface IModifierOperation {

    SerializableDataType<IModifierOperation> DATA_TYPE = SerializableDataType.registry(ApoliRegistries.MODIFIER_OPERATION, Apoli.MODID, true);

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
    double apply(Entity entity, Collection<SerializableData.Instance> instances, double base, double current);

    enum Phase {
        BASE, TOTAL
    }

}
