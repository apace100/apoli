package io.github.apace100.apoli.action.type.bientity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.action.type.meta.IfElseListMetaActionType;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import net.minecraft.entity.Entity;

import java.util.List;

public class IfElseListBiEntityActionType extends BiEntityActionType implements IfElseListMetaActionType<BiEntityActionContext, BiEntityConditionContext, BiEntityAction, BiEntityCondition> {

	private final List<ConditionedAction<BiEntityAction, BiEntityCondition>> conditionedActions;

	public IfElseListBiEntityActionType(List<ConditionedAction<BiEntityAction, BiEntityCondition>> conditionedActions) {
		this.conditionedActions = conditionedActions;
	}

	@Override
	protected void execute(Entity actor, Entity target) {
		executeActions(new BiEntityActionContext(actor, target));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return BiEntityActionTypes.IF_ELSE_LIST;
	}

	@Override
	public List<ConditionedAction<BiEntityAction, BiEntityCondition>> conditionedActions() {
		return conditionedActions;
	}

}
