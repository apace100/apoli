package io.github.apace100.apoli.action.type.bientity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.action.type.meta.ChoiceMetaActionType;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.WeightedList;

public class ChoiceBiEntityActionType extends BiEntityActionType implements ChoiceMetaActionType<BiEntityActionContext, BiEntityAction> {

	private final WeightedList<BiEntityAction> actions;

	public ChoiceBiEntityActionType(WeightedList<BiEntityAction> actions) {
		this.actions = actions;
	}

	@Override
	public void execute(Entity actor, Entity target) {
		executeActions(new BiEntityActionContext(actor, target));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return BiEntityActionTypes.CHOICE;
	}

	@Override
	public WeightedList<BiEntityAction> actions() {
		return actions;
	}

}
