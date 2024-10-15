package io.github.apace100.apoli.action.type.bientity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.action.type.meta.SideMetaActionType;
import net.minecraft.entity.Entity;

public class SideBiEntityActionType extends BiEntityActionType implements SideMetaActionType<BiEntityActionContext, BiEntityAction> {

	private final BiEntityAction action;
	private final Side side;

	public SideBiEntityActionType(BiEntityAction action, Side side) {
		this.action = action;
		this.side = side;
	}

	@Override
	public void execute(Entity actor, Entity target) {
		executeAction(new BiEntityActionContext(actor, target));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return BiEntityActionTypes.SIDE;
	}

	@Override
	public BiEntityAction action() {
		return action;
	}

	@Override
	public Side side() {
		return side;
	}

}
