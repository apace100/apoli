package io.github.apace100.apoli.condition.type.fluid;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.FluidConditionContext;
import io.github.apace100.apoli.condition.type.FluidConditionType;
import io.github.apace100.apoli.condition.type.FluidConditionTypes;
import org.jetbrains.annotations.NotNull;

public class StillFluidConditionType extends FluidConditionType {

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return FluidConditionTypes.STILL;
	}

	@Override
	public boolean test(FluidConditionContext context) {
		return context.fluidState().isStill();
	}

}
