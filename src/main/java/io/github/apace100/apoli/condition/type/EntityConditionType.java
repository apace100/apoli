package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.context.EntityContext;
import net.minecraft.entity.Entity;

public abstract class EntityConditionType extends AbstractConditionType<EntityContext, EntityCondition> {

	@Override
	public final boolean test(EntityContext context) {
		return test(context.entity());
	}

	public abstract boolean test(Entity entity);

}
