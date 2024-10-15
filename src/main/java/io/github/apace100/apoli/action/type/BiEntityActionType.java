package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import net.minecraft.entity.Entity;

public abstract class BiEntityActionType extends AbstractActionType<BiEntityActionContext, BiEntityAction> {

	@Override
	public final void accept(BiEntityActionContext context) {
		execute(context.actor(), context.target());
	}

	public abstract void execute(Entity actor, Entity target);

}
