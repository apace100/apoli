package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.recipe.ApoliRecipeSerializers;
import io.github.apace100.apoli.recipe.ModifiedCraftingRecipe;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
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

        if (recipe instanceof ModifiedCraftingRecipe) {
            throw new IllegalStateException("Using the type \"" + Registries.RECIPE_SERIALIZER.getId(ApoliRecipeSerializers.MODIFIED_CRAFTING) + "\" in recipes aren't allowed!");
        }

        else {
            return original;
        }

    }

}
