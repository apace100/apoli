package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import io.github.apace100.apoli.util.ModifiedCraftingRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(CraftingResultSlot.class)
public class CraftingResultSlotMixin {

    @Shadow @Final private CraftingInventory input;

    @Inject(method = "onTakeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getRemainingStacks(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Lnet/minecraft/util/collection/DefaultedList;"))
    private void testOnTakeItem(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if(!player.world.isClient) {
            PowerCraftingInventory pci = (PowerCraftingInventory)input;
            if(pci.getPower() instanceof ModifyCraftingPower mcp) {
                Optional<BlockPos> blockPos = ModifiedCraftingRecipe.getBlockFromInventory(input);
                mcp.executeActions(blockPos);
            }
        }
    }
}
