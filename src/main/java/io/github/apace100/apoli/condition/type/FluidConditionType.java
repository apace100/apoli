package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.FluidCondition;
import io.github.apace100.apoli.condition.context.FluidConditionContext;
import net.minecraft.fluid.FluidState;

public abstract class FluidConditionType extends AbstractConditionType<FluidConditionContext, FluidCondition> {

	@Override
	public final boolean test(FluidConditionContext context) {
		return test(context.fluidState());
	}

	public abstract boolean test(FluidState fluidState);

}
