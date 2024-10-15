package io.github.apace100.apoli.condition;

import io.github.apace100.apoli.condition.context.FluidConditionContext;
import io.github.apace100.apoli.condition.type.FluidConditionType;
import io.github.apace100.apoli.condition.type.FluidConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.fluid.FluidState;

public class FluidCondition extends AbstractCondition<FluidConditionContext, FluidConditionType> {

	public static final SerializableDataType<FluidCondition> DATA_TYPE = SerializableDataType.lazy(() -> ApoliDataTypes.condition("type", FluidConditionTypes.DATA_TYPE, FluidCondition::new));

	public FluidCondition(FluidConditionType conditionType, boolean inverted) {
		super(conditionType, inverted);
	}

	public FluidCondition(FluidConditionType conditionType) {
		this(conditionType, false);
	}

	public boolean test(FluidState fluidState) {
		return test(new FluidConditionContext(fluidState));
	}

}
