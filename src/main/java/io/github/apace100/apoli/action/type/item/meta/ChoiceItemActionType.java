package io.github.apace100.apoli.action.type.item.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.action.type.meta.ChoiceMetaActionType;
import net.minecraft.inventory.StackReference;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ChoiceItemActionType extends ItemActionType implements ChoiceMetaActionType<ItemActionContext, ItemAction> {

	private final WeightedList<ItemAction> actions;

	public ChoiceItemActionType(WeightedList<ItemAction> actions) {
		this.actions = actions;
	}

	@Override
	protected void execute(World world, StackReference stackReference) {
		executeActions(new ItemActionContext(world, stackReference));
	}

	@Override
	public @NotNull ActionConfiguration<?> getConfig() {
		return ItemActionTypes.CHOICE;
	}

	@Override
	public WeightedList<ItemAction> actions() {
		return actions;
	}

}
