package io.github.apace100.apoli.condition.context;

import io.github.apace100.apoli.util.context.TypeConditionContext;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public record ItemConditionContext(World world, ItemStack stack) implements TypeConditionContext {

}
