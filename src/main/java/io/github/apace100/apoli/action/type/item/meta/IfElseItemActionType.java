package io.github.apace100.apoli.action.type.item.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.action.type.meta.IfElseMetaActionType;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.condition.context.ItemConditionContext;
import net.minecraft.inventory.StackReference;
import net.minecraft.world.World;

import java.util.Optional;

public class IfElseItemActionType extends ItemActionType implements IfElseMetaActionType<ItemActionContext, ItemConditionContext, ItemAction, ItemCondition> {

	private final ItemCondition condition;

	private final ItemAction ifAction;
	private final Optional<ItemAction> elseAction;

	public IfElseItemActionType(ItemCondition condition, ItemAction ifAction, Optional<ItemAction> elseAction) {
		this.condition = condition;
		this.ifAction = ifAction;
		this.elseAction = elseAction;
	}

	@Override
	public void execute(World world, StackReference stackReference) {
		executeAction(new ItemActionContext(world, stackReference));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return ItemActionTypes.IF_ELSE;
	}

	@Override
	public ItemCondition condition() {
		return condition;
	}

	@Override
	public ItemAction ifAction() {
		return ifAction;
	}

	@Override
	public Optional<ItemAction> elseAction() {
		return elseAction;
	}

}
