package io.github.apace100.apoli.action.type.item.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.action.type.meta.IfElseListMetaActionType;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.condition.context.ItemConditionContext;
import net.minecraft.inventory.StackReference;
import net.minecraft.world.World;

import java.util.List;

public class IfElseListItemActionType extends ItemActionType implements IfElseListMetaActionType<ItemActionContext, ItemConditionContext, ItemAction, ItemCondition> {

	private final List<ConditionedAction<ItemAction, ItemCondition>> conditionedActions;

	public IfElseListItemActionType(List<ConditionedAction<ItemAction, ItemCondition>> conditionedActions) {
		this.conditionedActions = conditionedActions;
	}

	@Override
	protected void execute(World world, StackReference stackReference) {
		executeActions(new ItemActionContext(world, stackReference));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return ItemActionTypes.IF_ELSE_LIST;
	}

	@Override
	public List<ConditionedAction<ItemAction, ItemCondition>> conditionedActions() {
		return conditionedActions;
	}

}
