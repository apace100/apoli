package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.context.ItemActionContext;
import net.minecraft.inventory.StackReference;
import net.minecraft.world.World;

public abstract class ItemActionType extends AbstractActionType<ItemActionContext, ItemAction> {

	@Override
	public final void accept(ItemActionContext context) {
		execute(context.world(), context.stackReference());
	}

	public abstract void execute(World world, StackReference stackReference);

}
