package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import net.minecraft.entity.Entity;

public abstract class EntityActionType extends AbstractActionType<EntityActionContext, EntityAction> {

	@Override
	public final void accept(EntityActionContext context) {
		execute(context.entity());
	}

	protected abstract void execute(Entity entity);

}
