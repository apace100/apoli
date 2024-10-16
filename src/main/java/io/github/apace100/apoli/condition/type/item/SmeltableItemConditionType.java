package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.world.World;

public class SmeltableItemConditionType extends ItemConditionType {

    @Override
    public boolean test(World world, ItemStack stack) {
        return world.getRecipeManager()
            .getFirstMatch(RecipeType.SMELTING, new SingleStackRecipeInput(stack), world)
            .isPresent();
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return ItemConditionTypes.SMELTABLE;
    }

}
