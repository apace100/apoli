package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.util.requirement.BiEntityRequirement;

public abstract class BiEntityActionType extends ActionType<BiEntityActionContext, BiEntityAction> {

	@Override
	public BiEntityAction createAction() {
		return new BiEntityAction(this);
	}

	@Override
	public boolean shouldExecute(BiEntityActionContext context) {
		return switch (getRequirement()) {
			case BOTH ->
				context.actor() != null && context.target() != null;
			case EITHER ->
				context.actor() != null || context.target() != null;
			case DEFAULT ->
				super.shouldExecute(context);
		};
	}

	public BiEntityRequirement getRequirement() {
		return BiEntityRequirement.DEFAULT;
	}

}
