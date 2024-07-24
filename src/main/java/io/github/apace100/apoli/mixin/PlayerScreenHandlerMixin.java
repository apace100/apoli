package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.screen.PlayerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin {

    @ModifyExpressionValue(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/screen/ScreenHandler;II)Lnet/minecraft/inventory/CraftingInventory;"))
    private CraftingInventory apoli$cachePlayerToCraftingInventory(CraftingInventory original, PlayerInventory playerInventory) {

        if (original instanceof PowerCraftingInventory pci) {
            pci.apoli$setPlayer(playerInventory.player);
        }

        return original;

    }

}
