package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.PowerCraftingObject;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.type.RecipePowerType;
import io.github.apace100.apoli.recipe.PowerCraftingRecipe;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RecipeResultCollection.class)
public abstract class RecipeResultCollectionMixin {

    @ModifyExpressionValue(method = "computeCraftables", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeMatcher;match(Lnet/minecraft/recipe/Recipe;Lit/unimi/dsi/fastutil/ints/IntList;)Z"))
    private boolean apoli$accountForPowerRecipes(boolean original, RecipeMatcher recipeFinder, int gridWidth, int gridHeight, RecipeBook recipeBook, @Local RecipeEntry<?> recipeEntry) {

        if (original && recipeEntry.value() instanceof PowerCraftingRecipe(Identifier powerId, CraftingRecipe ignored) && recipeBook instanceof PowerCraftingObject pco) {

            Power power = PowerManager.getNullable(powerId);
            PowerHolderComponent component = PowerHolderComponent.getNullable(pco.apoli$getPlayer());

            return power != null
                && component != null
                && component.hasPower(power)
                && component.getPowerType(power) instanceof RecipePowerType;

        }

        else {
            return original;
        }

    }

}
