package io.github.apace100.apoli.util;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;

public class StackReferenceUtil implements StackReference {

    private final Entity entity;
    private final StackReference stackReference;

    private StackReferenceUtil(Entity entity, int mappedIndex) {
        this.entity = entity;
        this.stackReference = entity.getStackReference(mappedIndex);
    }

    public static StackReference of(Entity entity, int mappedIndex) {
        StackReferenceUtil util = new StackReferenceUtil(entity, mappedIndex);
        return util.stackReference == StackReference.EMPTY ? StackReference.EMPTY : util;
    }

    @Override
    public ItemStack get() {
        ItemStack stack = stackReference.get();
        stack.setHolder(entity);
        return stack;
    }

    @Override
    public boolean set(ItemStack stack) {
        stack.setHolder(entity);
        return stackReference.set(stack);
    }

}
