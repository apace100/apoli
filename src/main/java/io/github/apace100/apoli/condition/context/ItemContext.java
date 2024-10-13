package io.github.apace100.apoli.condition.context;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public record ItemContext(World world, ItemStack stack) {

}
