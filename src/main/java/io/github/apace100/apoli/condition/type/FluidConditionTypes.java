package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.condition.type.fluid.FluidConditionType;
import io.github.apace100.apoli.condition.type.fluid.InTagConditionType;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Predicate;

public class FluidConditionTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();
    public static final SerializableDataType<ConditionConfiguration<io.github.apace100.apoli.condition.type.FluidConditionType>> DATA_TYPE = SerializableDataType.registry(ApoliRegistries.FLUID_CONDITION_TYPE, Apoli.MODID, ALIASES, (configurations, id) -> "Fluid condition type \"" + id + "\" is undefined!");

    public static void register() {
        MetaConditionTypes.register(ApoliDataTypes.FLUID_CONDITION, FluidConditionTypes::register);
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

    @SuppressWarnings("unchecked")
    public static <CT extends io.github.apace100.apoli.condition.type.FluidConditionType> ConditionConfiguration<CT> register(ConditionConfiguration<CT> configuration) {

        ConditionConfiguration<io.github.apace100.apoli.condition.type.FluidConditionType> casted = (ConditionConfiguration<io.github.apace100.apoli.condition.type.FluidConditionType>) configuration;
        Registry.register(ApoliRegistries.FLUID_CONDITION_TYPE, casted.id(), casted);

        return configuration;

    }

}
