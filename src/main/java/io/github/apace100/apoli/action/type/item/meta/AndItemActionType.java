package io.github.apace100.apoli.action.type.item.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.action.type.meta.AndMetaActionType;
import net.minecraft.inventory.StackReference;
import net.minecraft.world.World;

import java.util.List;

public class AndItemActionType extends ItemActionType implements AndMetaActionType<ItemActionContext, ItemAction> {

	private final List<ItemAction> actions;

	public AndItemActionType(List<ItemAction> actions) {
		this.actions = actions;
	}

	@Override
	protected void execute(World world, StackReference stackReference) {
		executeActions(new ItemActionContext(world, stackReference));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return ItemActionTypes.AND;
	}

	@Override
	public List<ItemAction> actions() {
		return actions;
	}

}
