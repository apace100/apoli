package io.github.apace100.apoli.action.type.entity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.action.type.meta.IfElseListMetaActionType;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import net.minecraft.entity.Entity;

import java.util.List;

public class IfElseListEntityActionType extends EntityActionType implements IfElseListMetaActionType<EntityActionContext, EntityConditionContext, EntityAction, EntityCondition> {

	private final List<ConditionedAction<EntityAction, EntityCondition>> conditionedActions;

	public IfElseListEntityActionType(List<ConditionedAction<EntityAction, EntityCondition>> conditionedActions) {
		this.conditionedActions = conditionedActions;
	}

	@Override
	protected void execute(Entity entity) {
		executeActions(new EntityActionContext(entity));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return EntityActionTypes.IF_ELSE_LIST;
	}

	@Override
	public List<ConditionedAction<EntityAction, EntityCondition>> conditionedActions() {
		return conditionedActions;
	}

}
