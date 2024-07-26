package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.PowerCraftingBook;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.NeoRecipePower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.recipe.PowerCraftingRecipe;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RecipeResultCollection.class)
public abstract class RecipeResultCollectionMixin {

    @ModifyExpressionValue(method = "computeCraftables", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeMatcher;match(Lnet/minecraft/recipe/Recipe;Lit/unimi/dsi/fastutil/ints/IntList;)Z"))
    private boolean apoli$accountForPowerRecipes(boolean original, RecipeMatcher recipeFinder, int gridWidth, int gridHeight, RecipeBook recipeBook, @Local RecipeEntry<?> recipeEntry) {

        if (original && recipeEntry.value() instanceof PowerCraftingRecipe pcr && recipeBook instanceof PowerCraftingBook pcb && pcb.apoli$getPlayer() != null) {

            PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(pcb.apoli$getPlayer());
            PowerType<?> powerType = PowerTypeRegistry.getNullable(pcr.powerId());

            return powerType != null
                && component != null
                && component.getPower(powerType) instanceof NeoRecipePower recipePower
                && recipeEntry.id().equals(recipePower.getRecipeId());

        }

        else {
            return original;
        }

    }

}
