package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.access.SlotState;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import io.github.apace100.apoli.power.ModifyGrindstonePower;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.ModifiedCraftingRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.Optional;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @ModifyVariable(method = "internalOnSlotClick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/screen/slot/Slot;tryTakeStackRange(IILnet/minecraft/entity/player/PlayerEntity;)Ljava/util/Optional;", ordinal = 0))
    private Optional<ItemStack> performAfterGrindstoneActionsEmptyHand(Optional<ItemStack> value, int slotIndex, int button, SlotActionType actionType, PlayerEntity player, @Local Slot slot) {
        if (this instanceof PowerModifiedGrindstone pmg && value.isPresent() && slotIndex == 2) {
            List<ModifyGrindstonePower> applyingPowers = pmg.apoli$getAppliedPowers();
            if (applyingPowers != null) {
                StackReference reference = InventoryUtil.createStackReference(value.get());
                applyingPowers.forEach(mgp -> {
                    mgp.applyAfterGrindingItemAction(reference);
                    mgp.executeActions(pmg.apoli$getPos());
                });
                return Optional.of(reference.get());
            }
        } else if (slot instanceof CraftingResultSlot craftingResultSlot) {

            if (!(((CraftingResultSlotAccessor)craftingResultSlot).getInput() instanceof CraftingInventory craftingInventory)) {
                return value;
            }

            PowerCraftingInventory powerCraftingInventory = (PowerCraftingInventory) craftingInventory;
            SlotState slotState = (SlotState) craftingResultSlot;

            if (!(powerCraftingInventory.apoli$getPower() instanceof ModifyCraftingPower modifyCraftingPower)) {
                return value;
            }

            modifyCraftingPower.executeActions(ModifiedCraftingRecipe.getBlockFromInventory(craftingInventory));
            StackReference reference = InventoryUtil.createStackReference(value.get());
            if (slotState.apoli$getState().map(id -> !id.equals(ModifyCraftingPower.MODIFIED_RESULT_STACK)).orElse(true)) {
                modifyCraftingPower.applyAfterCraftingItemAction(reference);
            }

            slotState.apoli$setState(null);

            return Optional.of(reference.get());
        }
        return value;
    }

    @ModifyVariable(method = "internalOnSlotClick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/screen/slot/Slot;tryTakeStackRange(IILnet/minecraft/entity/player/PlayerEntity;)Ljava/util/Optional;", ordinal = 1))
    private Optional<ItemStack> performAfterGrindstoneActionsCombine(Optional<ItemStack> value, int slotIndex, int button, SlotActionType actionType, PlayerEntity player, @Local Slot slot) {
        if (this instanceof PowerModifiedGrindstone pmg && value.isPresent() && slotIndex == 2) {
            List<ModifyGrindstonePower> applyingPowers = pmg.apoli$getAppliedPowers();
            if (applyingPowers != null) {
                StackReference reference = InventoryUtil.createStackReference(value.get());
                applyingPowers.forEach(mgp -> {
                    mgp.applyAfterGrindingItemAction(reference);
                    mgp.executeActions(pmg.apoli$getPos());
                });
                return Optional.of(reference.get());
            }
        } else if (slot instanceof CraftingResultSlot craftingResultSlot) {

            if (!(((CraftingResultSlotAccessor)craftingResultSlot).getInput() instanceof CraftingInventory craftingInventory)) {
                return value;
            }

            PowerCraftingInventory powerCraftingInventory = (PowerCraftingInventory) craftingInventory;
            SlotState slotState = (SlotState) craftingResultSlot;

            if (!(powerCraftingInventory.apoli$getPower() instanceof ModifyCraftingPower modifyCraftingPower)) {
                return value;
            }

            modifyCraftingPower.executeActions(ModifiedCraftingRecipe.getBlockFromInventory(craftingInventory));
            StackReference reference = InventoryUtil.createStackReference(value.get());
            if (slotState.apoli$getState().map(id -> !id.equals(ModifyCraftingPower.MODIFIED_RESULT_STACK)).orElse(true)) {
                modifyCraftingPower.applyAfterCraftingItemAction(reference);
            }

            slotState.apoli$setState(null);

            return Optional.of(reference.get());
        }
        return value;
    }
}
