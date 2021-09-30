package io.github.apace100.apoli.util;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.Collection;

public interface PowerGrantingItem {

    Collection<StackPowerUtil.StackPower> getPowers(ItemStack stack, EquipmentSlot slot);
}
