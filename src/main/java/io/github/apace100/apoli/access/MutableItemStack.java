package io.github.apace100.apoli.access;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface MutableItemStack {

    void setItem(Item item);
    void setFrom(ItemStack stack);

}
