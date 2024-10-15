package io.github.apace100.apoli.action.context;

import io.github.apace100.apoli.condition.context.ItemConditionContext;
import io.github.apace100.apoli.util.context.TypeActionContext;
import net.minecraft.inventory.StackReference;
import net.minecraft.world.World;

public record ItemActionContext(World world, StackReference stackReference) implements TypeActionContext<ItemConditionContext> {

	@Override
	public ItemConditionContext conditionContext() {
		return new ItemConditionContext(world(), stackReference().get());
	}

}
