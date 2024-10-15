package io.github.apace100.apoli.action.context;

import net.minecraft.inventory.StackReference;
import net.minecraft.world.World;

public record ItemActionContext(World world, StackReference stackReference) {

}
