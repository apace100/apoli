package io.github.apace100.apoli.action.type.entity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.action.type.meta.SideMetaActionType;
import net.minecraft.entity.Entity;

public class SideEntityActionType extends EntityActionType implements SideMetaActionType<EntityActionContext, EntityAction> {

	private final EntityAction action;
	private final Side side;

	public SideEntityActionType(EntityAction action, Side side) {
		this.action = action;
		this.side = side;
	}

	@Override
	protected void execute(Entity entity) {
		executeAction(new EntityActionContext(entity));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return EntityActionTypes.SIDE;
	}

	@Override
	public EntityAction action() {
		return action;
	}

	@Override
	public Side side() {
		return side;
	}

}
