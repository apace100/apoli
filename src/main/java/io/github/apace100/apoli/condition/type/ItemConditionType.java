package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.condition.context.ItemConditionContext;

public abstract class ItemConditionType extends ConditionType<ItemConditionContext, ItemCondition> {

	@Override
	public ItemCondition createCondition(boolean inverted) {
		return new ItemCondition(this, inverted);
	}

}
