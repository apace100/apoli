package io.github.apace100.apoli.condition;

import io.github.apace100.apoli.condition.type.AbstractConditionType;
import io.github.apace100.apoli.util.context.TypeConditionContext;
import io.github.apace100.calio.util.Validatable;

import java.util.function.Predicate;

public abstract class AbstractCondition<T extends TypeConditionContext, CT extends AbstractConditionType<T, ?>> implements Predicate<T>, Validatable {

	private final CT conditionType;
	private final boolean inverted;

	public AbstractCondition(CT conditionType, boolean inverted) {

		this.conditionType = conditionType;
		this.inverted = inverted;

		//noinspection unchecked
		((AbstractConditionType<T, AbstractCondition<T, CT>>) this.conditionType).init(this);

	}

	@Override
	public boolean test(T context) {
		return isInverted() != getConditionType().test(context);
	}

	@Override
	public void validate() throws Exception {
		getConditionType().validate();
	}

	public final CT getConditionType() {
		return conditionType;
	}

	public final boolean isInverted() {
		return inverted;
	}

}
