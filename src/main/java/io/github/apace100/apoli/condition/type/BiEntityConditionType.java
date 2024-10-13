package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.context.BiEntityContext;
import net.minecraft.entity.Entity;

public abstract class BiEntityConditionType extends AbstractConditionType<BiEntityContext, BiEntityCondition> {

	@Override
	public final boolean test(BiEntityContext context) {
		return test(context.actor(), context.target());
	}

	public abstract boolean test(Entity actor, Entity target);

}
