package io.github.apace100.apoli.action.type.entity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.action.type.meta.DelayMetaActionType;
import net.minecraft.entity.Entity;

public class DelayEntityActionType extends EntityActionType implements DelayMetaActionType<EntityActionContext, EntityAction> {

	private final EntityAction action;
	private final int ticks;

	public DelayEntityActionType(EntityAction action, int ticks) {
		this.action = action;
		this.ticks = ticks;
	}

	@Override
	protected void execute(Entity entity) {
		executeAction(new EntityActionContext(entity));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return EntityActionTypes.DELAY;
	}

	@Override
	public EntityAction action() {
		return action;
	}

	@Override
	public int ticks() {
		return ticks;
	}

}
