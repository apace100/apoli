package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import net.minecraft.entity.Entity;

public abstract class BiEntityConditionType extends AbstractConditionType<BiEntityConditionContext, BiEntityCondition> {

	@Override
	public boolean test(BiEntityConditionContext context) {
		return test(context.actor(), context.target());
	}

	@Override
	public BiEntityCondition createCondition(boolean inverted) {
		return new BiEntityCondition(this, inverted);
	}

	public abstract boolean test(Entity actor, Entity target);

}
