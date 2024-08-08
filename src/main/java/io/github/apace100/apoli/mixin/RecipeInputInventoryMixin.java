package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RecipeInputInventory.class)
public interface RecipeInputInventoryMixin {

    @ModifyReturnValue(method = "createPositionedRecipeInput", at = @At("RETURN"))
    private CraftingRecipeInput.Positioned apoli$passCacheToPositionedInput(CraftingRecipeInput.Positioned original) {

        if ((RecipeInputInventory) this instanceof PowerCraftingInventory sourcePci && original.input() instanceof PowerCraftingInventory targetPci) {
            targetPci.apoli$setPowerType(sourcePci.apoli$getPowerType());
            targetPci.apoli$setPlayer(sourcePci.apoli$getPlayer());
        }

        return original;

    }

}
