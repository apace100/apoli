package io.github.apace100.apoli.action.type.entity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.action.type.meta.ChoiceMetaActionType;
import net.minecraft.entity.Entity;
import net.minecraft.util.collection.WeightedList;
import org.jetbrains.annotations.NotNull;

public class ChoiceEntityActionType extends EntityActionType implements ChoiceMetaActionType<EntityActionContext, EntityAction> {

	private final WeightedList<EntityAction> actions;

	public ChoiceEntityActionType(WeightedList<EntityAction> actions) {
		this.actions = actions;
	}

	@Override
	protected void execute(Entity entity) {
		executeActions(new EntityActionContext(entity));
	}

	@Override
	public @NotNull ActionConfiguration<?> getConfig() {
		return EntityActionTypes.CHOICE;
	}

	@Override
	public WeightedList<EntityAction> actions() {
		return actions;
	}

}
