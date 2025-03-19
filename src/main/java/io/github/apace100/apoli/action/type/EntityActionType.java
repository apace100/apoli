package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;

public abstract class EntityActionType extends ActionType<EntityActionContext, EntityAction> {

	@Override
	public EntityAction createAction() {
		return new EntityAction(this);
	}

	@Override
	public boolean shouldExecute(EntityActionContext context) {
		return context.entity() != null;
	}

}
