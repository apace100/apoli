package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import net.minecraft.entity.Entity;

public abstract class EntityConditionType extends AbstractConditionType<EntityConditionContext, EntityCondition> {

	@Override
	public final boolean test(EntityConditionContext context) {
		return test(context.entity());
	}

	public abstract boolean test(Entity entity);

}
