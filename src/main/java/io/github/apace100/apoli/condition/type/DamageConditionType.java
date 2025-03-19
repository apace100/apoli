package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.DamageCondition;
import io.github.apace100.apoli.condition.context.DamageConditionContext;

public abstract class DamageConditionType extends ConditionType<DamageConditionContext, DamageCondition> {

	@Override
	public DamageCondition createCondition(boolean inverted) {
		return new DamageCondition(this, inverted);
	}

}
