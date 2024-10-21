package io.github.apace100.apoli.action.type.bientity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.action.type.meta.AndMetaActionType;
import net.minecraft.entity.Entity;

import java.util.List;

public class AndBiEntityActionType extends BiEntityActionType implements AndMetaActionType<BiEntityActionContext, BiEntityAction> {

	private final List<BiEntityAction> actions;

	public AndBiEntityActionType(List<BiEntityAction> actions) {
		this.actions = actions;
	}

	@Override
	protected void execute(Entity actor, Entity target) {
		executeActions(new BiEntityActionContext(actor, target));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return BiEntityActionTypes.AND;
	}

	@Override
	public List<BiEntityAction> actions() {
		return actions;
	}

}
