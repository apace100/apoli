package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.FluidCondition;
import io.github.apace100.apoli.condition.context.FluidConditionContext;

public abstract class FluidConditionType extends ConditionType<FluidConditionContext, FluidCondition> {

	@Override
	public FluidCondition createCondition(boolean inverted) {
		return new FluidCondition(this, inverted);
	}

}
