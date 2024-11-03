package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.FluidCondition;
import io.github.apace100.apoli.condition.context.FluidConditionContext;
import net.minecraft.fluid.FluidState;

public abstract class FluidConditionType extends AbstractConditionType<FluidConditionContext, FluidCondition> {

	@Override
	public boolean test(FluidConditionContext context) {
		return test(context.fluidState());
	}

	@Override
	public FluidCondition createCondition(boolean inverted) {
		return new FluidCondition(this, inverted);
	}

	public abstract boolean test(FluidState fluidState);

}
