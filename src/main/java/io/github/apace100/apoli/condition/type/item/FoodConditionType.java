package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.power.type.EdibleItemPowerType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

public class FoodConditionType {

    public static boolean condition(ItemStack stack) {
        return EdibleItemPowerType.get(stack).isPresent()
            || stack.contains(DataComponentTypes.FOOD);
    }

}
