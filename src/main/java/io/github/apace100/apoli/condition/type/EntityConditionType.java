package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.context.EntityConditionContext;

public abstract class EntityConditionType extends ConditionType<EntityConditionContext, EntityCondition> {

	@Override
	public EntityCondition createCondition(boolean inverted) {
		return new EntityCondition(this, inverted);
	}

	@Override
	public boolean shouldTest(EntityConditionContext context) {
		return context.entity() != null;
	}

}
