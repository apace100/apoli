package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.recipe.Recipe;

public class RecipePower extends Power {

    private final Recipe<CraftingInventory> recipe;

    public RecipePower(PowerType<?> type, LivingEntity entity, Recipe<CraftingInventory> recipe) {
        super(type, entity);
        this.recipe = recipe;
    }

    public Recipe<CraftingInventory> getRecipe() {
        return recipe;
    }
}
