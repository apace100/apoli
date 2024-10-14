package io.github.apace100.apoli.condition.type.fluid.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.FluidCondition;
import io.github.apace100.apoli.condition.context.FluidContext;
import io.github.apace100.apoli.condition.type.FluidConditionType;
import io.github.apace100.apoli.condition.type.FluidConditionTypes;
import io.github.apace100.apoli.condition.type.meta.AllOfMetaConditionType;
import net.minecraft.fluid.FluidState;

import java.util.List;

public class AllOfFluidConditionType extends FluidConditionType implements AllOfMetaConditionType<FluidContext, FluidCondition> {

	private final List<FluidCondition> conditions;

	public AllOfFluidConditionType(List<FluidCondition> conditions) {
		this.conditions = prepareConditions(this, conditions);
	}

	@Override
	public boolean test(FluidState fluidState) {
		return AllOfMetaConditionType.condition(new FluidContext(fluidState), conditions());
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return FluidConditionTypes.ALL_OF;
	}

	@Override
	public List<FluidCondition> conditions() {
		return conditions;
	}

}