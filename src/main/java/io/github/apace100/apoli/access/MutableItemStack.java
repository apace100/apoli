package io.github.apace100.apoli.access;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface MutableItemStack {

    void setItem(Item item);
    void setFrom(ItemStack stack);

}
