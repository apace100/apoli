package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.power.type.ModifyCraftingPowerType;
import io.github.apace100.apoli.power.type.ModifyGrindstonePowerType;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.recipe.ModifiedCraftingRecipe;
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
    private Optional<ItemStack> apoli$performAfterCraftingActions(Optional<ItemStack> original, int slotIndex, int button, SlotActionType actionType, PlayerEntity player, @Local Slot slot) {

        if ((ScreenHandler) (Object) this instanceof PowerModifiedGrindstone pmg && original.isPresent() && slotIndex == 2) {

            List<ModifyGrindstonePowerType> applyingPowers = pmg.apoli$getAppliedPowers();
            if (applyingPowers == null || applyingPowers.isEmpty()) {
                return original;
            }

            StackReference stackReference = InventoryUtil.createStackReference(original.get());
            applyingPowers.forEach(mgpt -> mgpt.executeActions(pmg.apoli$getPos(), stackReference));

            return Optional.of(stackReference.get());

        }

        else if (original.isPresent() && slot instanceof CraftingResultSlot resultSlot) {

            if (!(((CraftingResultSlotAccessor) resultSlot).getInput() instanceof CraftingInventory craftingInventory)) {
                return original;
            }

            if (!(craftingInventory instanceof PowerCraftingInventory pci)) {
                return original;
            }

            List<ModifyCraftingPowerType> modifyCraftingPowers = pci.apoli$getPowerTypes()
                .stream()
                .filter(ModifyCraftingPowerType.class::isInstance)
                .map(ModifyCraftingPowerType.class::cast)
                .toList();

            if (modifyCraftingPowers.isEmpty()) {
                return original;
            }

            modifyCraftingPowers.forEach(mcpt -> mcpt.executeActions(ModifiedCraftingRecipe.getBlockFromInventory(craftingInventory)));
            StackReference stackReference = InventoryUtil.createStackReference(original.get());

            modifyCraftingPowers.forEach(mcpt -> mcpt.applyAfterCraftingItemAction(stackReference));
            return Optional.of(stackReference.get());

        }

        return original;

    }

}
