package io.github.apace100.apoli.mixin;

import com.google.common.collect.ImmutableMap;
import io.github.apace100.apoli.util.ModifiedCraftingRecipe;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Shadow protected abstract <C extends Inventory, T extends Recipe<C>> Map<Identifier, Recipe<C>> getAllOfType(RecipeType<T> type);

    @Inject(method = "getFirstMatch", at = @At("HEAD"), cancellable = true)
    private void prioritizeModifiedRecipes(RecipeType<Recipe<Inventory>> type, Inventory inventory, World world, CallbackInfoReturnable<Optional<Recipe<Inventory>>> cir) {
        Optional<Recipe<Inventory>> modifiedRecipe = this.getAllOfType(type).values().stream().flatMap((recipe) -> {
            return Util.stream(type.match(recipe, world, inventory));
        }).filter(r -> r.getClass() == ModifiedCraftingRecipe.class).findFirst();
        if(modifiedRecipe.isPresent()) {
            cir.setReturnValue(modifiedRecipe);
        }
    }
}
