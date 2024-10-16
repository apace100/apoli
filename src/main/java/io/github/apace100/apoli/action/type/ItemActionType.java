package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.action.context.ItemActionContext;
import io.github.apace100.apoli.power.type.ModifyEnchantmentLevelPowerType;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public abstract class ItemActionType extends AbstractActionType<ItemActionContext, ItemAction> {

	@Override
	public final void accept(ItemActionContext context) {

		StackReference stackReference = context.stackReference();
		if (stackReference == StackReference.EMPTY) {
			//	Skip empty stack references since they're immutable and don't have anything useful
			return;
		}

		//	Replace the stack of the stack reference with a "workable" empty stack if the said stack is
		//	an instance of ItemStack#EMPTY
		if (stackReference.get() == ItemStack.EMPTY) {
			stackReference.set(new ItemStack((Void) null));
		}

		//	Execute the action of this type
		execute(context.world(), context.stackReference());

		//	Restore the ItemStack#EMPTY stack of the stack reference afterward
		if (!ModifyEnchantmentLevelPowerType.isWorkableEmptyStack(stackReference) && stackReference.get().isEmpty()) {
			stackReference.set(ItemStack.EMPTY);
		}

	}

	public abstract void execute(World world, StackReference stackReference);

}
