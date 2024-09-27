package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyCraftingPowerType;
import io.github.apace100.apoli.power.type.RestrictArmorPowerType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin {

    @Shadow
    @Final
    private RecipeInputInventory craftingInput;

    @ModifyExpressionValue(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/screen/ScreenHandler;II)Lnet/minecraft/inventory/CraftingInventory;"))
    private CraftingInventory apoli$cachePlayerToCraftingInventory(CraftingInventory original, PlayerInventory playerInventory) {

        if (original instanceof PowerCraftingInventory pci) {
            pci.apoli$setPlayer(playerInventory.player);
        }

        return original;

    }

    @ModifyExpressionValue(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;hasStack()Z", ordinal = 0), slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/entity/EquipmentSlot$Type;HUMANOID_ARMOR:Lnet/minecraft/entity/EquipmentSlot$Type;")))
    private boolean apoli$disallowQuickMovingRestrictedWearables(boolean original, PlayerEntity player, @Local(ordinal = 1) ItemStack stackToInsert, @Local EquipmentSlot slot) {
        return original
            || PowerHolderComponent.hasPowerType(player, RestrictArmorPowerType.class, p -> p.doesRestrict(stackToInsert, slot));
    }

    @ModifyVariable(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/PlayerScreenHandler;insertItem(Lnet/minecraft/item/ItemStack;IIZ)Z", ordinal = 0), ordinal = 1)
    private ItemStack apoli$modifyResultStackOnQuickMove(ItemStack original, PlayerEntity player, int slotId, @Local Slot slot) {
        return ModifyCraftingPowerType.executeAfterCraftingAction(player, craftingInput, slot, original);
    }

}
