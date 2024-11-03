package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import net.minecraft.entity.Entity;

public abstract class BiEntityActionType extends AbstractActionType<BiEntityActionContext, BiEntityAction> {

	@Override
	public void accept(BiEntityActionContext context) {
		execute(context.actor(), context.target());
	}

	@Override
	public BiEntityAction createAction() {
		return new BiEntityAction(this);
	}

	protected abstract void execute(Entity actor, Entity target);

}
