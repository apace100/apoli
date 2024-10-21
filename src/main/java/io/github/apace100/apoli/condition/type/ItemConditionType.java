package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.condition.context.ItemConditionContext;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public abstract class ItemConditionType extends AbstractConditionType<ItemConditionContext, ItemCondition> {

	@Override
	public final boolean test(ItemConditionContext context) {
		return test(context.world(), context.stack());
	}

	public abstract boolean test(World world, ItemStack stack);

}
