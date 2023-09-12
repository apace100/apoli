package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {

    @Shadow @Final private ScreenHandlerContext context;

    @Shadow @Final private RecipeInputInventory input;

    @Inject(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/Optional;"))
    private static void clearPowerCraftingInventory(ScreenHandler handler, World world, PlayerEntity player, RecipeInputInventory inventory, CraftingResultInventory resultInventory, CallbackInfo ci) {
        if (inventory instanceof CraftingInventory craftingInventory) ((PowerCraftingInventory)craftingInventory).apoli$setPower(null);
    }

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void allowUsingViaPower(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if(context.get((world, pos) -> pos.equals(player.getBlockPos()), false)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void modifyOutputItems(PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> cir, ItemStack itemStack, Slot slot, ItemStack itemStack2) {
        if(input instanceof PowerCraftingInventory pci) {
            if(pci.apoli$getPower() instanceof ModifyCraftingPower mcp) {
                mcp.applyAfterCraftingItemAction(itemStack2);
            }
        }
    }
}
