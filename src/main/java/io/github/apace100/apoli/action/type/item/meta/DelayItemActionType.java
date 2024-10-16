package io.github.apace100.apoli.action.type.item.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.action.type.meta.DelayMetaActionType;
import net.minecraft.inventory.StackReference;
import net.minecraft.world.World;

public class DelayItemActionType extends ItemActionType implements DelayMetaActionType<ItemActionContext, ItemAction> {

	private final ItemAction action;
	private final int ticks;

	public DelayItemActionType(ItemAction action, int ticks) {
		this.action = action;
		this.ticks = ticks;
	}

	@Override
	public void execute(World world, StackReference stackReference) {
		executeAction(new ItemActionContext(world, stackReference));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return ItemActionTypes.DELAY;
	}

	@Override
	public ItemAction action() {
		return action;
	}

	@Override
	public int ticks() {
		return ticks;
	}

}
