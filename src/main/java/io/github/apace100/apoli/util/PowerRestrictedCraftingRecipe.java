package io.github.apace100.apoli.util;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.mixin.CraftingInventoryAccessor;
import io.github.apace100.apoli.mixin.CraftingScreenHandlerAccessor;
import io.github.apace100.apoli.mixin.PlayerScreenHandlerAccessor;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import io.github.apace100.apoli.power.RecipePower;
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
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PowerRestrictedCraftingRecipe extends SpecialCraftingRecipe {

    public static final RecipeSerializer<?> SERIALIZER = new SpecialRecipeSerializer<>(PowerRestrictedCraftingRecipe::new);

    public PowerRestrictedCraftingRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        return inventory instanceof CraftingInventory craftingInventory && getRecipePowers(craftingInventory)
            .stream()
            .anyMatch(rp -> rp.getRecipe().value().matches(craftingInventory, world));
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {

        if (!(inventory instanceof CraftingInventory craftingInventory)) {
            return ItemStack.EMPTY;
        }

        PlayerEntity playerEntity = getPlayerFromInventory(craftingInventory);
        if (playerEntity == null) {
            return ItemStack.EMPTY;
        }

        Optional<RecipePower> recipePower = getRecipePowers(craftingInventory)
            .stream()
            .filter(rp -> rp.getRecipe().value().matches(craftingInventory, playerEntity.getWorld()))
            .max(Comparator.comparing(RecipePower::getPriority));

        if (recipePower.isEmpty()) {
            return ItemStack.EMPTY;
        }

        RecipeEntry<Recipe<CraftingInventory>> recipe = recipePower.get().getRecipe();
        Identifier recipeId = recipe.id();

        ItemStack newResultStack = recipe.value().craft(craftingInventory, registryManager);
        Optional<ModifyCraftingPower> modifyCraftingPower = PowerHolderComponent.getPowers(playerEntity, ModifyCraftingPower.class)
            .stream()
            .filter(mcp -> mcp.doesApply(recipeId, newResultStack))
            .max(Comparator.comparing(ModifyCraftingPower::getPriority));

        if (modifyCraftingPower.isEmpty()) {
            return newResultStack;
        }

        ((PowerCraftingInventory) craftingInventory).apoli$setPower(modifyCraftingPower.get());
        return modifyCraftingPower.get().getNewResult(newResultStack);

    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private PlayerEntity getPlayerFromInventory(CraftingInventory inv) {
        ScreenHandler handler = ((CraftingInventoryAccessor)inv).getHandler();
        return getPlayerFromHandler(handler);
    }

    private List<RecipePower> getRecipePowers(CraftingInventory craftingInventory) {

        ScreenHandler screenHandler = ((CraftingInventoryAccessor) craftingInventory).getHandler();
        PlayerEntity player = getPlayerFromHandler(screenHandler);

        return PowerHolderComponent.getPowers(player, RecipePower.class);

    }

    private PlayerEntity getPlayerFromHandler(ScreenHandler screenHandler) {

        if(screenHandler instanceof CraftingScreenHandler) {
            return ((CraftingScreenHandlerAccessor)screenHandler).getPlayer();
        }

        if(screenHandler instanceof PlayerScreenHandler) {
            return ((PlayerScreenHandlerAccessor)screenHandler).getOwner();
        }

        return null;

    }

}
