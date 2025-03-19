package io.github.apace100.apoli.condition;

import io.github.apace100.apoli.condition.type.ConditionType;
import io.github.apace100.apoli.util.context.ConditionContext;
import io.github.apace100.calio.util.Validatable;

import java.util.function.Predicate;

public abstract class Condition<CX extends ConditionContext, CT extends ConditionType<CX, ?>> implements Predicate<CX>, Validatable {

	private final CT conditionType;
	private final boolean inverted;

	public Condition(CT conditionType, boolean inverted) {

		this.conditionType = conditionType;
		this.inverted = inverted;

		//noinspection unchecked
		((ConditionType<CX, Condition<CX, CT>>) this.conditionType).init(this);

	}

	@Override
	public boolean test(CX context) {
		return getType().shouldTest(context)
			&& isInverted() != getType().test(context);
	}

	@Override
	public void validate() throws Exception {
		getType().validate();
	}

	public final CT getType() {
		return conditionType;
	}

	public final boolean isInverted() {
		return inverted;
	}

}
