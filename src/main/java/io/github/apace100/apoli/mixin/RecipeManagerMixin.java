package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.util.ModifiedCraftingRecipe;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Shadow
    protected abstract <C extends Inventory, T extends Recipe<C>> Map<Identifier, RecipeEntry<T>> getAllOfType(RecipeType<T> type);

    @ModifyReturnValue(method = "getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/Optional;", at = @At("RETURN"))
    private <C extends Inventory, T extends Recipe<C>> Optional<RecipeEntry<T>> apoli$prioritizeModifiedRecipes(Optional<RecipeEntry<T>> original, RecipeType<T> type, C inventory, World world) {
        return this.getAllOfType(type)
            .values()
            .stream()
            .filter(entry -> entry.value() instanceof ModifiedCraftingRecipe
                          && entry.value().matches(inventory, world))
            .findFirst()
            .or(() -> original);
    }

    @ModifyReturnValue(method = "getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;Lnet/minecraft/util/Identifier;)Ljava/util/Optional;", at = @At("RETURN"))
    private <C extends Inventory, T extends Recipe<C>> Optional<Pair<Identifier, RecipeEntry<T>>> apoli$prioritizeModifiedRecipesOnCache(Optional<Pair<Identifier, RecipeEntry<T>>> original, RecipeType<T> type, C inventory, World world, @Nullable Identifier id) {

        Map<Identifier, RecipeEntry<T>> recipes = this.getAllOfType(type);
        if (id != null) {

            RecipeEntry<T> entry = recipes.get(id);
            T recipe = entry.value();

            if (recipe instanceof ModifiedCraftingRecipe && recipe.matches(inventory, world)) {
                return Optional.of(Pair.of(id, entry));
            }

        }

        return recipes.entrySet()
            .stream()
            .filter(e -> e.getValue().value() instanceof ModifiedCraftingRecipe
                      && e.getValue().value().matches(inventory, world))
            .findFirst()
            .map(e -> Pair.of(e.getKey(), e.getValue()))
            .or(() -> original);

    }

}
