package io.github.apace100.apoli.condition.type.fluid;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;

public class InTagConditionType {

    public static boolean condition(FluidState fluidState, TagKey<Fluid> fluidTag) {
        return fluidState.isIn(fluidTag);
    }

    public static ConditionTypeFactory<FluidState> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("in_tag"),
            new SerializableData()
                .add("tag", SerializableDataTypes.FLUID_TAG),
            (data, fluidState) -> condition(fluidState,
                data.get("tag")
            )
        );
    }

}
