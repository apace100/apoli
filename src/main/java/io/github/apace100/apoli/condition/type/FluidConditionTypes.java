package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.FluidCondition;
import io.github.apace100.apoli.condition.type.fluid.FluidFluidConditionType;
import io.github.apace100.apoli.condition.type.fluid.InTagFluidConditionType;
import io.github.apace100.apoli.condition.type.fluid.meta.AllOfFluidConditionType;
import io.github.apace100.apoli.condition.type.fluid.meta.AnyOfFluidConditionType;
import io.github.apace100.apoli.condition.type.fluid.meta.ConstantFluidConditionType;
import io.github.apace100.apoli.condition.type.fluid.meta.RandomChanceFluidConditionType;
import io.github.apace100.apoli.condition.type.meta.AllOfMetaConditionType;
import io.github.apace100.apoli.condition.type.meta.AnyOfMetaConditionType;
import io.github.apace100.apoli.condition.type.meta.ConstantMetaConditionType;
import io.github.apace100.apoli.condition.type.meta.RandomChanceMetaConditionType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.registry.Registry;

public class FluidConditionTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();
    public static final SerializableDataType<ConditionConfiguration<io.github.apace100.apoli.condition.type.FluidConditionType>> DATA_TYPE = SerializableDataType.registry(ApoliRegistries.FLUID_CONDITION_TYPE, Apoli.MODID, ALIASES, (configurations, id) -> "Fluid condition type \"" + id + "\" is undefined!");

    public static final ConditionConfiguration<AllOfFluidConditionType> ALL_OF = register(AllOfMetaConditionType.createConfiguration(FluidCondition.DATA_TYPE, AllOfFluidConditionType::new));
    public static final ConditionConfiguration<AnyOfFluidConditionType> ANY_OF = register(AnyOfMetaConditionType.createConfiguration(FluidCondition.DATA_TYPE, AnyOfFluidConditionType::new));
    public static final ConditionConfiguration<ConstantFluidConditionType> CONSTANT = register(ConstantMetaConditionType.createConfiguration(ConstantFluidConditionType::new));
    public static final ConditionConfiguration<RandomChanceFluidConditionType> RANDOM_CHANCE = register(RandomChanceMetaConditionType.createConfiguration(RandomChanceFluidConditionType::new));

    public static final ConditionConfiguration<FluidFluidConditionType> FLUID = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("fluid"), FluidFluidConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<InTagFluidConditionType> IN_TAG = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("in_tag"), InTagFluidConditionType.DATA_FACTORY));

    public static void register() {

    }

    @SuppressWarnings("unchecked")
    public static <CT extends io.github.apace100.apoli.condition.type.FluidConditionType> ConditionConfiguration<CT> register(ConditionConfiguration<CT> configuration) {

        ConditionConfiguration<io.github.apace100.apoli.condition.type.FluidConditionType> casted = (ConditionConfiguration<io.github.apace100.apoli.condition.type.FluidConditionType>) configuration;
        Registry.register(ApoliRegistries.FLUID_CONDITION_TYPE, casted.id(), casted);

        return configuration;

    }

}
