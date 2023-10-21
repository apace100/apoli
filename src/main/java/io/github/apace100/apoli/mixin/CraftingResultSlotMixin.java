package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.access.SlotState;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import io.github.apace100.apoli.util.ModifiedCraftingRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingResultSlot.class)
public class CraftingResultSlotMixin extends Slot {

    @Shadow @Final private RecipeInputInventory input;

    @Shadow @Final private PlayerEntity player;

    public CraftingResultSlotMixin(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Inject(method = "onCrafted(Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;onCraft(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;I)V"))
    private void apoli$executeActionsOnCraftingItem(ItemStack stack, CallbackInfo ci) {

        if (this.player.getWorld().isClient || !(input instanceof CraftingInventory craftingInventory)) {
            return;
        }

        PowerCraftingInventory powerCraftingInventory = (PowerCraftingInventory) craftingInventory;
        SlotState slotState = (SlotState) this;

        if (!(powerCraftingInventory.apoli$getPower() instanceof ModifyCraftingPower modifyCraftingPower)) {
            return;
        }

        modifyCraftingPower.executeActions(ModifiedCraftingRecipe.getBlockFromInventory(craftingInventory));
        if (slotState.apoli$getState().map(id -> !id.equals(ModifyCraftingPower.MODIFIED_RESULT_STACK)).orElse(true)) {
            modifyCraftingPower.applyAfterCraftingItemAction(stack);
        }

        slotState.apoli$setState(null);

    }

}
