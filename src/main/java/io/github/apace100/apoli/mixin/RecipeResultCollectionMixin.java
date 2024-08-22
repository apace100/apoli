package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.PowerCraftingObject;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.type.RecipePowerType;
import io.github.apace100.apoli.recipe.PowerCraftingRecipe;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(RecipeResultCollection.class)
public abstract class RecipeResultCollectionMixin {

    @ModifyExpressionValue(method = "computeCraftables", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeMatcher;match(Lnet/minecraft/recipe/Recipe;Lit/unimi/dsi/fastutil/ints/IntList;)Z"))
    private boolean apoli$accountForPowerRecipes(boolean original, RecipeMatcher recipeFinder, int gridWidth, int gridHeight, RecipeBook recipeBook, @Local RecipeEntry<?> recipeEntry) {

        if (original && recipeEntry.value() instanceof PowerCraftingRecipe pcr && recipeBook instanceof PowerCraftingObject pco && pco.apoli$getPlayer() != null) {

            PowerHolderComponent component = PowerHolderComponent.KEY.get(pco.apoli$getPlayer());
            RecipePowerType recipePowerType = PowerManager.getOptional(pcr.powerId())
                .map(component::getPowerType)
                .filter(RecipePowerType.class::isInstance)
                .map(RecipePowerType.class::cast)
                .orElse(null);

            return recipePowerType != null
                && Objects.equals(recipePowerType.getRecipeId(), recipeEntry.id());

        }

        else {
            return original;
        }

    }

}
