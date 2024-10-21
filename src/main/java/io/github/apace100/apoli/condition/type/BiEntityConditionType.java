package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import net.minecraft.entity.Entity;

public abstract class BiEntityConditionType extends AbstractConditionType<BiEntityConditionContext, BiEntityCondition> {

	@Override
	public final boolean test(BiEntityConditionContext context) {
		return test(context.actor(), context.target());
	}

	public abstract boolean test(Entity actor, Entity target);

}
