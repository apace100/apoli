package io.github.apace100.apoli.condition.factory;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.type.fluid.FluidConditionType;
import io.github.apace100.apoli.condition.type.fluid.InTagConditionType;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Predicate;

public class FluidConditions {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    public static void register() {
        MetaConditions.register(ApoliDataTypes.FLUID_CONDITION, FluidConditions::register);
        register(createSimpleFactory(Apoli.identifier("empty"), FluidState::isEmpty));
        register(createSimpleFactory(Apoli.identifier("still"), FluidState::isStill));
        register(InTagConditionType.getFactory());
        register(FluidConditionType.getFactory());
    }

    public static ConditionTypeFactory<FluidState> createSimpleFactory(Identifier id, Predicate<FluidState> condition) {
        return new ConditionTypeFactory<>(id, new SerializableData(), (data, fluidState) -> condition.test(fluidState));
    }

    public static <F extends ConditionTypeFactory<FluidState>> F register(F conditionFactory) {
        return Registry.register(ApoliRegistries.FLUID_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }

}
