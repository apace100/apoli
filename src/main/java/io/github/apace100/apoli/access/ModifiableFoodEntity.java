package io.github.apace100.apoli.access;

import io.github.apace100.apoli.power.type.EdibleItemPowerType;
import io.github.apace100.apoli.power.type.ModifyFoodPowerType;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface ModifiableFoodEntity {

    ItemStack apoli$getOriginalFoodStack();
    void apoli$setOriginalFoodStack(ItemStack original);

    List<ModifyFoodPowerType> apoli$getCurrentModifyFoodPowers();
    void apoli$setCurrentModifyFoodPowers(List<ModifyFoodPowerType> powers);

    EdibleItemPowerType apoli$getEdibleItemPower();
    void apoli$setEdibleItemPower(EdibleItemPowerType power);

}
