package io.github.apace100.apoli.util;

import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.recipe.ModifiedCraftingRecipe;
import io.github.apace100.apoli.recipe.PowerCraftingRecipe;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;

public class RecipeUtil {

	public static <R extends Recipe<?>> DataResult<R> validateRecipe(@NotNull R recipe) {
		return switch (recipe) {
			case ModifiedCraftingRecipe modifiedCraftingRecipe ->
				createInternalOnlyError(modifiedCraftingRecipe.getSerializer());
			case PowerCraftingRecipe powerCraftingRecipe ->
				createInternalOnlyError(powerCraftingRecipe.getSerializer());
			default -> DataResult.success(recipe);
		};
	}

	public static <R extends Recipe<?>> DataResult<CraftingRecipe> validateCraftingRecipe(@NotNull R recipe) {
		return validateRecipe(recipe).flatMap(r -> r instanceof CraftingRecipe craftingRecipe
			? DataResult.success(craftingRecipe)
			: DataResult.error(() -> "Recipe is not a crafting recipe!"));
	}

	private static <R> DataResult<R> createInternalOnlyError(RecipeSerializer<?> serializer) {
		return DataResult.error(() -> "Recipe type \"" + Registries.RECIPE_SERIALIZER.getId(serializer) + "\" is for internal use only!");
	}

}
