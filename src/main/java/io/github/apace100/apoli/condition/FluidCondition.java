package io.github.apace100.apoli.condition;

import io.github.apace100.apoli.condition.context.FluidContext;
import io.github.apace100.apoli.condition.type.FluidConditionType;
import io.github.apace100.apoli.condition.type.FluidConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import net.minecraft.fluid.FluidState;

public class FluidCondition extends AbstractCondition<FluidContext, FluidConditionType> {

	public static final CompoundSerializableDataType<FluidCondition> DATA_TYPE = ApoliDataTypes.condition("type", FluidConditionTypes.DATA_TYPE, FluidCondition::new);

	public FluidCondition(FluidConditionType conditionType, boolean inverted) {
		super(conditionType, inverted);
	}

	public FluidCondition(FluidConditionType conditionType) {
		this(conditionType, false);
	}

	public boolean test(FluidState fluidState) {
		return test(new FluidContext(fluidState));
	}

}
