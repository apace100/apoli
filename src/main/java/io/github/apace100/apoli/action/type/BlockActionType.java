package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.BlockActionContext;

public abstract class BlockActionType extends ActionType<BlockActionContext, BlockAction> {

	@Override
	public BlockAction createAction() {
		return new BlockAction(this);
	}

}
