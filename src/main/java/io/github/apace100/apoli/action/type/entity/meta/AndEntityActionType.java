package io.github.apace100.apoli.action.type.entity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.action.type.meta.AndMetaActionType;
import net.minecraft.entity.Entity;

import java.util.List;

public class AndEntityActionType extends EntityActionType implements AndMetaActionType<EntityActionContext, EntityAction> {

	private final List<EntityAction> actions;

	public AndEntityActionType(List<EntityAction> actions) {
		this.actions = actions;
	}

	@Override
	protected void execute(Entity entity) {
		executeActions(new EntityActionContext(entity));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return EntityActionTypes.AND;
	}

	@Override
	public List<EntityAction> actions() {
		return actions;
	}

}
