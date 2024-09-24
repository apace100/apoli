package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.recipe.ModifiedCraftingRecipe;
import io.github.apace100.apoli.recipe.PowerCraftingRecipe;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @ModifyReturnValue(method = "getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/recipe/input/RecipeInput;Lnet/minecraft/world/World;Lnet/minecraft/recipe/RecipeEntry;)Ljava/util/Optional;", at = @At("RETURN"))
    private Optional<RecipeEntry<?>> apoli$modifyCraftingRecipe(Optional<RecipeEntry<?>> original, RecipeType<?> type, RecipeInput input, World world) {
        return original.map(entry -> {

            Identifier id = entry.id();
            Recipe<?> recipe = entry.value();

            if (recipe instanceof CraftingRecipe craftingRecipe && ModifiedCraftingRecipe.canModify(id, craftingRecipe, input)) {
                return new RecipeEntry<>(id, new ModifiedCraftingRecipe(id, craftingRecipe));
            }

            else {
                return entry;
            }

        });
    }

    @ModifyExpressionValue(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At(value = "NEW", target = "(Lnet/minecraft/util/Identifier;Lnet/minecraft/recipe/Recipe;)Lnet/minecraft/recipe/RecipeEntry;"))
    private RecipeEntry<?> apoli$modifyCraftingRecipe(RecipeEntry<?> original, @Local Identifier id, @Local Recipe<?> recipe) {
        return switch (recipe) {
            case ModifiedCraftingRecipe modifiedCraftingRecipe ->
                throw apoli$createNotAllowedError(modifiedCraftingRecipe.getSerializer());
            case PowerCraftingRecipe powerCraftingRecipe ->
                throw apoli$createNotAllowedError(powerCraftingRecipe.getSerializer());
            default ->
                original;
        };
    }

    @Unique
    private RuntimeException apoli$createNotAllowedError(RecipeSerializer<?> serializer) {
        return new IllegalStateException("Recipe type \"" + Registries.RECIPE_SERIALIZER.getId(serializer) + "\" is only used internally and cannot be used in recipes!");
    }

}
