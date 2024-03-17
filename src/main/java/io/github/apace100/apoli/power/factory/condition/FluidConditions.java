package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registry;

public class FluidConditions {

    public static void register() {
        MetaConditions.register(ApoliDataTypes.FLUID_CONDITION, FluidConditions::register);
        register(new ConditionFactory<>(Apoli.identifier("empty"), new SerializableData(),
            (data, fluid) -> fluid.isEmpty()));
        register(new ConditionFactory<>(Apoli.identifier("still"), new SerializableData(),
            (data, fluid) -> fluid.isStill()));
        register(new ConditionFactory<>(Apoli.identifier("in_tag"), new SerializableData()
            .add("tag", SerializableDataTypes.FLUID_TAG),
            (data, fluid) -> fluid.getRegistryEntry().isIn(data.get("tag"))));
        register(new ConditionFactory<>(Apoli.identifier("fluid"), new SerializableData()
            .add("fluid", SerializableDataTypes.FLUID),
            (data, fluid) -> fluid.getFluid() == data.get("fluid")));
    }

    private static void register(ConditionFactory<FluidState> conditionFactory) {
        Registry.register(ApoliRegistries.FLUID_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
