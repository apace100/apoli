package io.github.apace100.apoli.condition.type.fluid.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.FluidConditionType;
import io.github.apace100.apoli.condition.type.FluidConditionTypes;
import io.github.apace100.apoli.condition.type.meta.ConstantMetaConditionType;
import net.minecraft.fluid.FluidState;

public class ConstantFluidConditionType extends FluidConditionType implements ConstantMetaConditionType {

	private final boolean value;

	public ConstantFluidConditionType(boolean value) {
		this.value = value;
	}

	@Override
	public boolean test(FluidState fluidState) {
		return value();
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return FluidConditionTypes.CONSTANT;
	}

	@Override
	public boolean value() {
		return value;
	}

}
