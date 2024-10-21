package io.github.apace100.apoli.action.type.entity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.action.type.meta.ChanceMetaActionType;
import net.minecraft.entity.Entity;

import java.util.Optional;

public class ChanceEntityActionType extends EntityActionType implements ChanceMetaActionType<EntityActionContext, EntityAction> {

	private final EntityAction successAction;
	private final Optional<EntityAction> failAction;

	private final float chance;

	public ChanceEntityActionType(EntityAction successAction, Optional<EntityAction> failAction, float chance) {
		this.successAction = successAction;
		this.failAction = failAction;
		this.chance = chance;
	}

	@Override
	protected void execute(Entity entity) {
		executeAction(new EntityActionContext(entity));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return EntityActionTypes.CHANCE;
	}

	@Override
	public EntityAction successAction() {
		return successAction;
	}

	@Override
	public Optional<EntityAction> failAction() {
		return failAction;
	}

	@Override
	public float chance() {
		return chance;
	}

}
