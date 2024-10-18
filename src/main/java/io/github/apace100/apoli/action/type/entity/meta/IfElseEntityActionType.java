package io.github.apace100.apoli.action.type.entity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.action.type.meta.IfElseMetaActionType;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import net.minecraft.entity.Entity;

import java.util.Optional;

public class IfElseEntityActionType extends EntityActionType implements IfElseMetaActionType<EntityActionContext, EntityConditionContext, EntityAction, EntityCondition> {

	private final EntityCondition condition;

	private final EntityAction ifAction;
	private final Optional<EntityAction> elseAction;

	public IfElseEntityActionType(EntityCondition condition, EntityAction ifAction, Optional<EntityAction> elseAction) {
		this.condition = condition;
		this.ifAction = ifAction;
		this.elseAction = elseAction;
	}

	@Override
	protected void execute(Entity entity) {
		executeAction(new EntityActionContext(entity));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return EntityActionTypes.IF_ELSE;
	}

	@Override
	public EntityCondition condition() {
		return condition;
	}

	@Override
	public EntityAction ifAction() {
		return ifAction;
	}

	@Override
	public Optional<EntityAction> elseAction() {
		return elseAction;
	}

}
