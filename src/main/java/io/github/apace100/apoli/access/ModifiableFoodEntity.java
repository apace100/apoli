package io.github.apace100.apoli.access;

import io.github.apace100.apoli.power.ModifyFoodPower;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface ModifiableFoodEntity {

    ItemStack getOriginalFoodStack();
    void setOriginalFoodStack(ItemStack original);

    List<ModifyFoodPower> getCurrentModifyFoodPowers();
    void setCurrentModifyFoodPowers(List<ModifyFoodPower> powers);
}
