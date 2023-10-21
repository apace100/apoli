package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.access.ScreenHandlerUsabilityOverride;
import io.github.apace100.apoli.access.SlotState;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin extends AbstractRecipeScreenHandler<RecipeInputInventory> implements ScreenHandlerUsabilityOverride {

    @Shadow
    @Final
    private RecipeInputInventory input;

    @Unique
    private boolean apoli$canUse = false;

    @Override
    public boolean apoli$canUse() {
        return this.apoli$canUse;
    }

    @Override
    public void apoli$canUse(boolean canUse) {
        this.apoli$canUse = canUse;
    }

    public CraftingScreenHandlerMixin(ScreenHandlerType<?> screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    @Inject(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/Optional;"))
    private static void apoli$clearPowerCraftingInventory(ScreenHandler handler, World world, PlayerEntity player, RecipeInputInventory inventory, CraftingResultInventory resultInventory, CallbackInfo ci) {

        if (inventory instanceof CraftingInventory craftingInventory) {
            ((PowerCraftingInventory) craftingInventory).apoli$setPower(null);
        }

    }

    @ModifyReturnValue(method = "canUse", at = @At("RETURN"))
    private boolean apoli$allowUsingViaPower(boolean original, PlayerEntity playerEntity) {
        return original || this.apoli$canUse();
    }

    @Inject(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER))
    private void apoli$modifyResultStackOnQuickMove(PlayerEntity player, int slotIndex, CallbackInfoReturnable<ItemStack> cir, @Local(ordinal = 1) ItemStack itemStack2, @Local Slot slot) {

        if (!(input instanceof PowerCraftingInventory pci && pci.apoli$getPower() instanceof ModifyCraftingPower mcp)) {
            return;
        }

        //  Check if the player's inventory have room for the item stack
        int availableSlotIndex = player.getInventory().getOccupiedSlotWithRoomForStack(itemStack2);

        //  If the player's inventory don't have room for the item stack, check for empty slots
        if (availableSlotIndex == -1) {
            availableSlotIndex = player.getInventory().getEmptySlot();
        }

        //  If there's either room for the item stack in the player's inventory or if the item stack
        //  can be inserted into the player's inventory, execute the item action
        if (availableSlotIndex != -1 && slot instanceof CraftingResultSlot) {

            ((SlotState) slot).apoli$setState(ModifyCraftingPower.MODIFIED_RESULT_STACK);
            mcp.applyAfterCraftingItemAction(itemStack2);

        }

    }

}
