package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.access.ScreenHandlerUsabilityOverride;
import io.github.apace100.apoli.power.type.ModifyCraftingPowerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
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

import java.util.LinkedList;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin extends AbstractRecipeScreenHandler<CraftingRecipeInput, CraftingRecipe> implements ScreenHandlerUsabilityOverride {

    @Shadow
    @Final
    private RecipeInputInventory input;

    @Shadow @Final private PlayerEntity player;
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

    @ModifyExpressionValue(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At(value = "NEW", target = "(Lnet/minecraft/screen/ScreenHandler;II)Lnet/minecraft/inventory/CraftingInventory;"))
    private CraftingInventory apoli$cachePlayerToCraftingInventory(CraftingInventory original, int syncId, PlayerInventory playerInventory) {

        if (original instanceof PowerCraftingInventory pci) {
            pci.apoli$setPlayer(playerInventory.player);
        }

        return original;

    }

    @Inject(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/recipe/input/RecipeInput;Lnet/minecraft/world/World;Lnet/minecraft/recipe/RecipeEntry;)Ljava/util/Optional;"))
    private static void apoli$clearPowerCraftingInventory(ScreenHandler handler, World world, PlayerEntity player, RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, @Nullable RecipeEntry<CraftingRecipe> recipe, CallbackInfo ci) {

        if (craftingInventory instanceof PowerCraftingInventory pci) {
            pci.apoli$setPowerTypes(new LinkedList<>());
        }

    }

    @ModifyReturnValue(method = "canUse", at = @At("RETURN"))
    private boolean apoli$allowUsingViaPower(boolean original, PlayerEntity playerEntity) {
        return original || this.apoli$canUse();
    }

    @ModifyVariable(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/CraftingScreenHandler;insertItem(Lnet/minecraft/item/ItemStack;IIZ)Z", ordinal = 0), ordinal = 1)
    private ItemStack apoli$modifyResultStackOnQuickMove(ItemStack original, PlayerEntity player, int slotId, @Local Slot slot) {
        return ModifyCraftingPowerType.executeAfterCraftingAction(player, input, slot, original);
    }

}
