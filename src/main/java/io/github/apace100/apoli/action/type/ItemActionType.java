package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.context.ItemActionContext;
import net.minecraft.inventory.StackReference;

public abstract class ItemActionType extends ActionType<ItemActionContext, ItemAction> {

	@Override
	public ItemAction createAction() {
		return new ItemAction(this);
	}

	@Override
	public boolean shouldExecute(ItemActionContext context) {
		return context.stackReference() != StackReference.EMPTY;
	}

}
