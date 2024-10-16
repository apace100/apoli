package io.github.apace100.apoli.action.type.item.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.action.type.meta.SideMetaActionType;
import net.minecraft.inventory.StackReference;
import net.minecraft.world.World;

public class SideItemActionType extends ItemActionType implements SideMetaActionType<ItemActionContext, ItemAction> {

	private final ItemAction action;
	private final Side side;

	public SideItemActionType(ItemAction action, Side side) {
		this.action = action;
		this.side = side;
	}

	@Override
	public void execute(World world, StackReference stackReference) {
		executeAction(new ItemActionContext(world, stackReference));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return ItemActionTypes.SIDE;
	}

	@Override
	public ItemAction action() {
		return action;
	}

	@Override
	public Side side() {
		return side;
	}

}
