package io.github.apace100.apoli.util;

import com.google.common.collect.Lists;
import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.mixin.CraftingInventoryAccessor;
import io.github.apace100.apoli.mixin.CraftingScreenHandlerAccessor;
import io.github.apace100.apoli.mixin.PlayerScreenHandlerAccessor;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class ModifiedCraftingRecipe extends SpecialCraftingRecipe {

    public static final RecipeSerializer<?> SERIALIZER = new SpecialRecipeSerializer<>(ModifiedCraftingRecipe::new);

    public ModifiedCraftingRecipe(Identifier id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        Optional<CraftingRecipe> original = getOriginalMatch(inv);
        if(original.isEmpty()) {
            return false;
        }
        return getRecipes(inv).stream().anyMatch(r -> r.doesApply(inv, original.get()));
    }

    @Override
    public ItemStack craft(CraftingInventory inv) {
        PlayerEntity player = getPlayerFromInventory(inv);
        if(player != null) {
            Optional<CraftingRecipe> original = getOriginalMatch(inv);
            if(original.isPresent()) {
                Optional<ModifyCraftingPower> optional = getRecipes(inv).stream().filter(r -> r.doesApply(inv, original.get())).findFirst();
                if(optional.isPresent()) {
                    ItemStack result = optional.get().getNewResult(inv, original.get());
                    ((PowerCraftingInventory)inv).setPower(optional.get());
                    return result;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static PlayerEntity getPlayerFromInventory(CraftingInventory inv) {
        ScreenHandler handler = ((CraftingInventoryAccessor)inv).getHandler();
        return getPlayerFromHandler(handler);
    }

    public static Optional<BlockPos> getBlockFromInventory(CraftingInventory inv) {
        ScreenHandler handler = ((CraftingInventoryAccessor)inv).getHandler();
        if(handler instanceof CraftingScreenHandler) {
            return ((CraftingScreenHandlerAccessor)handler).getContext().get((world, blockPos) -> blockPos);
        }
        return Optional.empty();
    }

    private List<ModifyCraftingPower> getRecipes(CraftingInventory inv) {
        ScreenHandler handler = ((CraftingInventoryAccessor)inv).getHandler();
        PlayerEntity player = getPlayerFromHandler(handler);
        if(player != null) {
            return PowerHolderComponent.getPowers(player, ModifyCraftingPower.class);
        }
        return Lists.newArrayList();
    }

    private Optional<CraftingRecipe> getOriginalMatch(CraftingInventory inv) {
        ScreenHandler handler = ((CraftingInventoryAccessor)inv).getHandler();
        PlayerEntity player = getPlayerFromHandler(handler);
        if(player != null && player.getServer() != null) {
            List<CraftingRecipe> recipes = player.getServer().getRecipeManager().listAllOfType(RecipeType.CRAFTING);
            return recipes.stream()
                .filter(cr -> !(cr instanceof ModifiedCraftingRecipe)
                    && cr.matches(inv, player.world))
                .findFirst();
        }
        return Optional.empty();
    }

    private static PlayerEntity getPlayerFromHandler(ScreenHandler screenHandler) {
        if(screenHandler instanceof CraftingScreenHandler) {
            return ((CraftingScreenHandlerAccessor)screenHandler).getPlayer();
        }
        if(screenHandler instanceof PlayerScreenHandler) {
            return ((PlayerScreenHandlerAccessor)screenHandler).getOwner();
        }
        return null;
    }
}
