package io.github.apace100.apoli.condition.type.fluid;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;

public class FluidConditionType {

    public static boolean condition(FluidState fluidState, Fluid fluid) {
        return fluidState.getFluid() == fluid;
    }

    public static ConditionTypeFactory<FluidState> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("fluid"),
            new SerializableData()
                .add("fluid", SerializableDataTypes.FLUID),
            (data, fluidState) -> condition(fluidState,
                data.get("fluid")
            )
        );
    }

}
