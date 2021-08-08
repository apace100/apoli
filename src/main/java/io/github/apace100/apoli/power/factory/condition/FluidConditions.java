package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.Tag;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class FluidConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("constant"), new SerializableData()
            .add("value", SerializableDataTypes.BOOLEAN),
            (data, fluid) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("conditions", ApoliDataTypes.FLUID_CONDITIONS),
            (data, fluid) -> ((List<ConditionFactory<FluidState>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(fluid)
            )));
        register(new ConditionFactory<>(Apoli.identifier("or"), new SerializableData()
            .add("conditions", ApoliDataTypes.FLUID_CONDITIONS),
            (data, fluid) -> ((List<ConditionFactory<FluidState>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(fluid)
            )));

        register(new ConditionFactory<>(Apoli.identifier("empty"), new SerializableData(),
            (data, fluid) -> fluid.isEmpty()));
        register(new ConditionFactory<>(Apoli.identifier("still"), new SerializableData(),
            (data, fluid) -> fluid.isStill()));
        register(new ConditionFactory<>(Apoli.identifier("in_tag"), new SerializableData()
            .add("tag", SerializableDataTypes.FLUID_TAG),
            (data, fluid) -> fluid.isIn((Tag<Fluid>)data.get("tag"))));
        register(new ConditionFactory<>(Apoli.identifier("fluid"), new SerializableData()
            .add("fluid", ApoliDataTypes.FLUID),
            (data, fluid) -> fluid.getFluid() == data.get("fluid")));
    }

    private static void register(ConditionFactory<FluidState> conditionFactory) {
        Registry.register(ApoliRegistries.FLUID_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
