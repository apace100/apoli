package io.github.apace100.apoli.recipe;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.util.LegacyPowerCraftingRecipe;
import io.github.apace100.apoli.util.ModifiedCraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ApoliRecipeSerializers {

    @Deprecated(forRemoval = true)
    public static final RecipeSerializer<LegacyPowerCraftingRecipe> LEGACY_POWER_CRAFTING = register("legacy_power_crafting", new SpecialRecipeSerializer<>(LegacyPowerCraftingRecipe::new));

    public static final RecipeSerializer<PowerCraftingRecipe> POWER_CRAFTING = register("power_crafting", new PowerCraftingRecipe.Serializer());
    public static final RecipeSerializer<ModifiedCraftingRecipe> MODIFIED_CRAFTING = register("modified_crafting", new SpecialRecipeSerializer<>(ModifiedCraftingRecipe::new));

    public static void register() {

    }

    public static <R extends Recipe<?>, S extends RecipeSerializer<R>> S register(String path, S serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Apoli.identifier(path), serializer);
    }

}
