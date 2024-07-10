package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.screen.PlayerScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin {

    @Shadow
    @Final
    private RecipeInputInventory craftingInput;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void apoli$cachePlayerToCraftingInventory(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {

        if (this.craftingInput instanceof PowerCraftingInventory pci) {
            pci.apoli$setPlayer(owner);
        }

    }

}
