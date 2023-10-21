package io.github.apace100.apoli.util;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.mixin.CraftingInventoryAccessor;
import io.github.apace100.apoli.mixin.CraftingScreenHandlerAccessor;
import io.github.apace100.apoli.mixin.PlayerScreenHandlerAccessor;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ModifiedCraftingRecipe extends SpecialCraftingRecipe {

    public static final RecipeSerializer<?> SERIALIZER = new SpecialRecipeSerializer<>(ModifiedCraftingRecipe::new);

    public ModifiedCraftingRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {

        if (!(inventory instanceof CraftingInventory craftingInventory)) {
            return false;
        }

        RecipeEntry<CraftingRecipe> originalRecipe = getOriginalRecipe(craftingInventory)
            .orElse(null);
        if (originalRecipe == null) {
            return false;
        }

        Identifier id = originalRecipe.id();
        ItemStack resultStack = originalRecipe.value().craft(craftingInventory, world.getRegistryManager());

        return getModifyCraftingPowers(craftingInventory)
            .stream()
            .anyMatch(mcp -> mcp.doesApply(id, resultStack));

    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {

        ItemStack newResultStack = ItemStack.EMPTY;
        if (!(inventory instanceof CraftingInventory craftingInventory)) {
            return newResultStack;
        }

        PlayerEntity playerEntity = getPlayerFromInventory(craftingInventory);
        if (playerEntity == null) {
            return newResultStack;
        }

        RecipeEntry<CraftingRecipe> originalRecipe = getOriginalRecipe(craftingInventory)
            .orElse(null);
        if (originalRecipe == null) {
            return newResultStack;
        }

        Identifier recipeId = originalRecipe.id();
        ItemStack resultStack = originalRecipe.value().craft(craftingInventory, registryManager);

        Optional<ModifyCraftingPower> modifyCraftingPower = getModifyCraftingPowers(craftingInventory)
            .stream()
            .filter(mcp -> mcp.doesApply(recipeId, resultStack))
            .max(Comparator.comparing(ModifyCraftingPower::getPriority));

        if (modifyCraftingPower.isEmpty()) {
            return resultStack;
        }

        newResultStack = modifyCraftingPower.get().getNewResult(resultStack);
        ((PowerCraftingInventory) craftingInventory).apoli$setPower(modifyCraftingPower.get());

        return newResultStack;

    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static PlayerEntity getPlayerFromInventory(CraftingInventory craftingInventory) {
        ScreenHandler handler = ((CraftingInventoryAccessor) craftingInventory).getHandler();
        return getPlayerFromHandler(handler);
    }

    public static Optional<BlockPos> getBlockFromInventory(CraftingInventory craftingInventory) {

        ScreenHandler handler = ((CraftingInventoryAccessor) craftingInventory).getHandler();
        if (handler instanceof CraftingScreenHandler craftingScreenHandler) {
            return ((CraftingScreenHandlerAccessor) craftingScreenHandler).getContext().get((world, pos) -> pos);
        }

        return Optional.empty();

    }

    private List<ModifyCraftingPower> getModifyCraftingPowers(CraftingInventory craftingInventory) {

        ScreenHandler handler = ((CraftingInventoryAccessor)craftingInventory).getHandler();
        PlayerEntity player = getPlayerFromHandler(handler);

        return PowerHolderComponent.getPowers(player, ModifyCraftingPower.class);

    }

    private Optional<RecipeEntry<CraftingRecipe>> getOriginalRecipe(CraftingInventory craftingInventory) {

        ScreenHandler screenHandler = ((CraftingInventoryAccessor) craftingInventory).getHandler();
        PlayerEntity playerEntity = getPlayerFromHandler(screenHandler);

        if (playerEntity == null || playerEntity.getServer() == null) {
            return Optional.empty();
        }

        return playerEntity.getServer().getRecipeManager().listAllOfType(RecipeType.CRAFTING)
            .stream()
            .filter(entry -> !(entry.value() instanceof ModifiedCraftingRecipe)
                          && entry.value().matches(craftingInventory, playerEntity.getWorld()))
            .findFirst();

    }

    private static PlayerEntity getPlayerFromHandler(ScreenHandler screenHandler) {

        if(screenHandler instanceof CraftingScreenHandler) {
            return ((CraftingScreenHandlerAccessor) screenHandler).getPlayer();
        }

        if(screenHandler instanceof PlayerScreenHandler) {
            return ((PlayerScreenHandlerAccessor) screenHandler).getOwner();
        }

        return null;

    }

}
