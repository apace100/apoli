package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.FluidCondition;
import io.github.apace100.apoli.condition.context.FluidContext;
import net.minecraft.fluid.FluidState;

public abstract class FluidConditionType extends AbstractConditionType<FluidContext, FluidCondition> {

	@Override
	public final boolean test(FluidContext context) {
		return test(context.fluidState());
	}

	public abstract boolean test(FluidState fluidState);

}
