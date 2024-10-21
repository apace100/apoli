package io.github.apace100.apoli.condition.type.fluid;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.FluidConditionType;
import io.github.apace100.apoli.condition.type.FluidConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;

public class FluidFluidConditionType extends FluidConditionType {

    public static final TypedDataObjectFactory<FluidFluidConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("fluid", SerializableDataTypes.FLUID),
        data -> new FluidFluidConditionType(
            data.get("fluid")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("fluid", conditionType.fluid)
    );

    private final Fluid fluid;

    public FluidFluidConditionType(Fluid fluid) {
        this.fluid = fluid;
    }

    @Override
    public boolean test(FluidState fluidState) {
        return fluidState.isOf(fluid);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return FluidConditionTypes.FLUID;
    }

}
