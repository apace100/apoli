package io.github.apace100.apoli.util;

import io.github.apace100.apoli.power.EdibleItemPower;
import net.minecraft.entity.Entity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public class ItemStackUtil {

    public static Optional<FoodComponent> getFoodComponent(ItemStack stack, Entity holder, boolean useModifications) {

        if (useModifications) {
            return Optional.ofNullable(EdibleItemPower.get(stack, holder)
                .map(EdibleItemPower::getFoodComponent)
                .orElseGet(stack::getFoodComponent));
        }

        else {
            return Optional.ofNullable(stack.getFoodComponent());
        }

    }

    public static Optional<FoodComponent> getFoodComponent(ItemStack stack, boolean useModifications) {

        if (useModifications) {
            return Optional.ofNullable(EdibleItemPower.get(stack)
                .map(EdibleItemPower::getFoodComponent)
                .orElseGet(stack::getFoodComponent));
        }

        else {
            return Optional.ofNullable(stack.getFoodComponent());
        }

    }

}
