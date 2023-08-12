package io.github.apace100.apoli.access;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface MutableItemStack {

    void apoli$setItem(Item item);

    void apoli$setFrom(ItemStack stack);
}
