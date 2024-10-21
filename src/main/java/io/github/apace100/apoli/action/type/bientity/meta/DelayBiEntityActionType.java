package io.github.apace100.apoli.action.type.bientity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.action.type.meta.DelayMetaActionType;
import net.minecraft.entity.Entity;

public class DelayBiEntityActionType extends BiEntityActionType implements DelayMetaActionType<BiEntityActionContext, BiEntityAction> {

	private final BiEntityAction action;
	private final int ticks;

	public DelayBiEntityActionType(BiEntityAction action, int ticks) {
		this.action = action;
		this.ticks = ticks;
	}

	@Override
	protected void execute(Entity actor, Entity target) {
		executeAction(new BiEntityActionContext(actor, target));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return BiEntityActionTypes.DELAY;
	}

	@Override
	public BiEntityAction action() {
		return action;
	}

	@Override
	public int ticks() {
		return ticks;
	}

}
