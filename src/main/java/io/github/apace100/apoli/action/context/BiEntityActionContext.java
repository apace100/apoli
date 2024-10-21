package io.github.apace100.apoli.action.context;

import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import io.github.apace100.apoli.util.context.TypeActionContext;
import net.minecraft.entity.Entity;

public record BiEntityActionContext(Entity actor, Entity target) implements TypeActionContext<BiEntityConditionContext> {

	@Override
	public BiEntityConditionContext conditionContext() {
		return new BiEntityConditionContext(actor(), target());
	}

}
