package io.github.apace100.apoli.condition.type.fluid.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.FluidCondition;
import io.github.apace100.apoli.condition.context.FluidContext;
import io.github.apace100.apoli.condition.type.FluidConditionType;
import io.github.apace100.apoli.condition.type.FluidConditionTypes;
import io.github.apace100.apoli.condition.type.meta.AnyOfMetaConditionType;
import io.github.apace100.apoli.power.type.PowerType;
import net.minecraft.fluid.FluidState;

import java.util.List;
import java.util.Optional;

public class AnyOfFluidConditionType extends FluidConditionType implements AnyOfMetaConditionType<FluidContext, FluidCondition> {

	private final List<FluidCondition> conditions;

	public AnyOfFluidConditionType(List<FluidCondition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public boolean test(FluidState fluidState) {
		return AnyOfMetaConditionType.condition(new FluidContext(fluidState), conditions());
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return FluidConditionTypes.ANY_OF;
	}

	@Override
	public List<FluidCondition> conditions() {
		return conditions;
	}

	@Override
	public void setPowerType(Optional<PowerType> powerType) {
		super.setPowerType(powerType);
		propagatePowerType(powerType);
	}

}
