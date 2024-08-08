package io.github.apace100.apoli.util;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.mixin.CraftingInventoryAccessor;
import io.github.apace100.apoli.mixin.CraftingScreenHandlerAccessor;
import io.github.apace100.apoli.power.type.ModifyCraftingPowerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ModifiedCraftingRecipe extends SpecialCraftingRecipe {

    public static final RecipeSerializer<?> SERIALIZER = new SpecialRecipeSerializer<>(ModifiedCraftingRecipe::new);

    public ModifiedCraftingRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {

        RecipeEntry<CraftingRecipe> originalRecipe = getOriginalRecipe(input);
        if (originalRecipe == null) {
            return false;
        }

        Identifier id = originalRecipe.id();
        ItemStack resultStack = originalRecipe.value().craft(input, world.getRegistryManager());

        return getModifyCraftingPowers(input)
            .stream()
            .anyMatch(mcp -> mcp.doesApply(id, resultStack));

    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {

        if (!(input instanceof PowerCraftingInventory pci)) {
            return ItemStack.EMPTY;
        }

        ItemStack newResultStack = ItemStack.EMPTY;
        PlayerEntity playerEntity = pci.apoli$getPlayer();
        
        if (playerEntity == null) {
            return newResultStack;
        }

        RecipeEntry<CraftingRecipe> originalRecipe = getOriginalRecipe(input);
        if (originalRecipe == null) {
            return newResultStack;
        }

        Identifier recipeId = originalRecipe.id();
        ItemStack resultStack = originalRecipe.value().craft(input, lookup);

        Optional<ModifyCraftingPowerType> modifyCraftingPower = getModifyCraftingPowers(input)
            .stream()
            .filter(mcp -> mcp.doesApply(recipeId, resultStack))
            .max(Comparator.comparing(ModifyCraftingPowerType::getPriority));

        if (modifyCraftingPower.isEmpty()) {
            return resultStack;
        }

        newResultStack = modifyCraftingPower.get().getNewResult(InventoryUtil.createStackReference(resultStack)).get();
        pci.apoli$setPowerType(modifyCraftingPower.get());

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

    public static Optional<BlockPos> getBlockFromInventory(CraftingInventory craftingInventory) {

        ScreenHandler handler = ((CraftingInventoryAccessor) craftingInventory).getHandler();
        if (handler instanceof CraftingScreenHandler craftingScreenHandler) {
            return ((CraftingScreenHandlerAccessor) craftingScreenHandler).getContext().get((world, pos) -> pos);
        }

        return Optional.empty();

    }

    private static List<ModifyCraftingPowerType> getModifyCraftingPowers(CraftingRecipeInput input) {
        return input instanceof PowerCraftingInventory pci
            ? PowerHolderComponent.getPowerTypes(pci.apoli$getPlayer(), ModifyCraftingPowerType.class)
            : Lists.newArrayList();
    }

    @Nullable
    private static RecipeEntry<CraftingRecipe> getOriginalRecipe(CraftingRecipeInput input) {

        if (!(input instanceof PowerCraftingInventory pci)) {
            return null;
        }

        PlayerEntity playerEntity = pci.apoli$getPlayer();
        if (playerEntity == null || playerEntity.getServer() == null) {
            return null;
        }

        return playerEntity.getServer().getRecipeManager().listAllOfType(RecipeType.CRAFTING)
            .stream()
            .filter(entry -> !(entry.value() instanceof ModifiedCraftingRecipe)
                && entry.value().matches(input, playerEntity.getWorld()))
            .findFirst()
            .orElse(null);

    }

}
