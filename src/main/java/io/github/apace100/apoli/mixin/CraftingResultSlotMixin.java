package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import io.github.apace100.apoli.util.ModifiedCraftingRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingResultSlot.class)
public class CraftingResultSlotMixin {

    @Shadow @Final private RecipeInputInventory input;

    @Inject(method = "onTakeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getRemainingStacks(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Lnet/minecraft/util/collection/DefaultedList;"))
    private void apoli$executeActionsOnTakingItem(PlayerEntity player, ItemStack stack, CallbackInfo ci) {

        if (player.getWorld().isClient || !(input instanceof CraftingInventory craftingInventory)) {
            return;
        }

        PowerCraftingInventory powerCraftingInventory = (PowerCraftingInventory) craftingInventory;
        if (powerCraftingInventory.apoli$getPower() instanceof ModifyCraftingPower modifyCraftingPower) {

            modifyCraftingPower.executeActions(ModifiedCraftingRecipe.getBlockFromInventory(craftingInventory));
            modifyCraftingPower.applyAfterCraftingItemAction(stack);

        }

    }

}
