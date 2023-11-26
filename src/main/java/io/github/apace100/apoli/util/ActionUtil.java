package io.github.apace100.apoli.util;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class ActionUtil {

    public static void executeEntityDependentItemAction(Entity entity, World world, ItemStack stack, Consumer<Pair<World, ItemStack>> action) {
        StackReference reference = InventoryUtil.setToWorkableEmpty(entity, null, stack, 0);
        stack = reference.get();
        action.accept(new Pair<>(world, stack));
        reference.set(stack);
        InventoryUtil.setToGlobalEmpty(entity, null, reference.get(), 0);
    }

}
