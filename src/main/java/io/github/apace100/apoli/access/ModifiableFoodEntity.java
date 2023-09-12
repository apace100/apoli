package io.github.apace100.apoli.access;

import io.github.apace100.apoli.power.ModifyFoodPower;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface ModifiableFoodEntity {

    ItemStack apoli$getOriginalFoodStack();
    void apoli$setOriginalFoodStack(ItemStack original);

    List<ModifyFoodPower> apoli$getCurrentModifyFoodPowers();
    void apoli$setCurrentModifyFoodPowers(List<ModifyFoodPower> powers);
}
