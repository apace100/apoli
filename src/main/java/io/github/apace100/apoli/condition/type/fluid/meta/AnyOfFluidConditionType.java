package io.github.apace100.apoli.condition.type.fluid.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.FluidCondition;
import io.github.apace100.apoli.condition.context.FluidConditionContext;
import io.github.apace100.apoli.condition.type.FluidConditionType;
import io.github.apace100.apoli.condition.type.FluidConditionTypes;
import io.github.apace100.apoli.condition.type.meta.AnyOfMetaConditionType;
import net.minecraft.fluid.FluidState;

import java.util.List;

public class AnyOfFluidConditionType extends FluidConditionType implements AnyOfMetaConditionType<FluidConditionContext, FluidCondition> {

	private final List<FluidCondition> conditions;

	public AnyOfFluidConditionType(List<FluidCondition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public boolean test(FluidState fluidState) {
		return testConditions(new FluidConditionContext(fluidState));
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return FluidConditionTypes.ANY_OF;
	}

	@Override
	public List<FluidCondition> conditions() {
		return conditions;
	}

}
