package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.access.ScreenHandlerUsabilityOverride;
import io.github.apace100.apoli.access.SlotState;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import io.github.apace100.apoli.util.InventoryUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin extends AbstractRecipeScreenHandler<CraftingRecipeInput, CraftingRecipe> implements ScreenHandlerUsabilityOverride {

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

    private CraftingScreenHandlerMixin(ScreenHandlerType screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("TAIL"))
    private void apoli$cachePlayerToCraftingInventory(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {

        if (this.input instanceof PowerCraftingInventory pci) {
            pci.apoli$setPlayer(playerInventory.player);
        }

    }

    @Inject(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/recipe/input/RecipeInput;Lnet/minecraft/world/World;Lnet/minecraft/recipe/RecipeEntry;)Ljava/util/Optional;"))
    private static void apoli$clearPowerCraftingInventory(ScreenHandler handler, World world, PlayerEntity player, RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, @Nullable RecipeEntry<CraftingRecipe> recipe, CallbackInfo ci) {

        if (craftingInventory instanceof PowerCraftingInventory pci) {
            pci.apoli$setPower(null);
        }

    }

    @ModifyReturnValue(method = "canUse", at = @At("RETURN"))
    private boolean apoli$allowUsingViaPower(boolean original, PlayerEntity playerEntity) {
        return original || this.apoli$canUse();
    }

    @ModifyVariable(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER), ordinal = 1)
    private ItemStack apoli$modifyResultStackOnQuickMove(ItemStack itemStack2, PlayerEntity player, int slotIndex, @Local Slot slot) {

        if (!(input instanceof PowerCraftingInventory pci && pci.apoli$getPower() instanceof ModifyCraftingPower mcp)) {
            return itemStack2;
        }

        //  Check if the player's inventory have room for the item stack
        int availableSlotIndex = player.getInventory().getOccupiedSlotWithRoomForStack(itemStack2);

        //  If the player's inventory don't have room for the item stack, check for empty slots
        if (availableSlotIndex == -1) {
            availableSlotIndex = player.getInventory().getEmptySlot();
        }

        StackReference reference = InventoryUtil.createStackReference(itemStack2);

        //  If there's either room for the item stack in the player's inventory or if the item stack
        //  can be inserted into the player's inventory, execute the item action
        if (availableSlotIndex != -1 && slot instanceof CraftingResultSlot) {

            ((SlotState) slot).apoli$setState(ModifyCraftingPower.MODIFIED_RESULT_STACK);
            mcp.applyAfterCraftingItemAction(reference);

        }

        return reference.get();

    }

}
