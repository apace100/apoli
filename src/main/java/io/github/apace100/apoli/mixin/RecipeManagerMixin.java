package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.util.ModifiedCraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Shadow protected abstract <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeEntry<T>> getAllOfType(RecipeType<T> type);

    @ModifyReturnValue(method = "getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/recipe/input/RecipeInput;Lnet/minecraft/world/World;Lnet/minecraft/recipe/RecipeEntry;)Ljava/util/Optional;", at = @At("RETURN"))
    private <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeEntry<T>> apoli$tset(Optional<RecipeEntry<T>> original, RecipeType<T> type, I input, World world) {
        return this.getAllOfType(type)
            .stream()
            .filter(entry -> entry.value() instanceof ModifiedCraftingRecipe
                && entry.value().matches(input, world))
            .findFirst()
            .or(() -> original);
    }

}
