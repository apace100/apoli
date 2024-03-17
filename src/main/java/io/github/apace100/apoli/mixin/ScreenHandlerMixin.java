package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
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

import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @ModifyExpressionValue(method = "internalOnSlotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;tryTakeStackRange(IILnet/minecraft/entity/player/PlayerEntity;)Ljava/util/Optional;"))
    private Optional<ItemStack> apoli$performAfterGrindstoneActions(Optional<ItemStack> original, int slotIndex, int button, SlotActionType actionType, PlayerEntity player, @Local Slot slot) {

        if ((ScreenHandler) (Object) this instanceof PowerModifiedGrindstone pmg && original.isPresent() && slotIndex == 2) {

            List<ModifyGrindstonePower> applyingPowers = pmg.apoli$getAppliedPowers();
            if (applyingPowers == null || applyingPowers.isEmpty()) {
                return original;
            }

            StackReference stackReference = InventoryUtil.createStackReference(original.get());
            applyingPowers.forEach(mgp -> {
                mgp.applyAfterGrindingItemAction(stackReference);
                mgp.executeActions(pmg.apoli$getPos());
            });

            return Optional.of(stackReference.get());

        }

        else if (original.isPresent() && slot instanceof CraftingResultSlot resultSlot && resultSlot instanceof SlotState slotState) {

            if (!(((CraftingResultSlotAccessor) resultSlot).getInput() instanceof CraftingInventory craftingInventory)) {
                return original;
            }

            if (!(craftingInventory instanceof PowerCraftingInventory pci) || !(pci.apoli$getPower() instanceof ModifyCraftingPower modifyCraftingPower)) {
                return original;
            }

            modifyCraftingPower.executeActions(ModifiedCraftingRecipe.getBlockFromInventory(craftingInventory));
            StackReference stackReference = InventoryUtil.createStackReference(original.get());

            if (slotState.apoli$getState().map(id -> !ModifyCraftingPower.MODIFIED_RESULT_STACK.equals(id)).orElse(true)) {
                modifyCraftingPower.applyAfterCraftingItemAction(stackReference);
            }

            slotState.apoli$setState(null);
            return Optional.of(stackReference.get());

        }

        return original;

    }

}
