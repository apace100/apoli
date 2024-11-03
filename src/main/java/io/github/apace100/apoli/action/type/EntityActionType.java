package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import net.minecraft.entity.Entity;

public abstract class EntityActionType extends AbstractActionType<EntityActionContext, EntityAction> {

	@Override
	public void accept(EntityActionContext context) {

		Entity entity = context.entity();

		if (entity != null) {
			execute(entity);
		}

	}

	@Override
	public EntityAction createAction() {
		return new EntityAction(this);
	}

	protected abstract void execute(Entity entity);

}
