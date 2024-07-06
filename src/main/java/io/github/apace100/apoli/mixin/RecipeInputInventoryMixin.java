package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RecipeInputInventory.class)
public interface RecipeInputInventoryMixin {

    @ModifyReturnValue(method = "createRecipeInput", at = @At("RETURN"))
    private CraftingRecipeInput apoli$passCache(CraftingRecipeInput original) {

        if ((RecipeInputInventory) this instanceof PowerCraftingInventory sourcePci && original instanceof PowerCraftingInventory targetPci) {
            targetPci.apoli$setPower(sourcePci.apoli$getPower());
            targetPci.apoli$setPlayer(sourcePci.apoli$getPlayer());
        }

        return original;

    }

}
