package io.github.apace100.apoli.util;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import io.github.apace100.apoli.power.RecipePower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.apache.commons.compress.utils.Lists;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

//  TODO: Move away from using a single recipe instance -eggohito
public class PowerRestrictedCraftingRecipe extends SpecialCraftingRecipe {

    public static final RecipeSerializer<?> SERIALIZER = new SpecialRecipeSerializer<>(PowerRestrictedCraftingRecipe::new);

    public PowerRestrictedCraftingRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        return getRecipePowers(input)
            .stream()
            .anyMatch(rp -> rp.getRecipe().value() instanceof CraftingRecipe craftingRecipe
                && craftingRecipe.matches(input, world));
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {

        if (!(input instanceof PowerCraftingInventory pci)) {
            return ItemStack.EMPTY;
        }

        PlayerEntity playerEntity = pci.apoli$getPlayer();
        if (playerEntity == null) {
            return ItemStack.EMPTY;
        }

        Optional<RecipePower> recipePower = getRecipePowers(input)
            .stream()
            .filter(rp -> rp.getRecipe().value() instanceof CraftingRecipe craftingRecipe
                && craftingRecipe.matches(input, playerEntity.getWorld()))
            .max(Comparator.comparing(RecipePower::getPriority));

        if (recipePower.isEmpty()) {
            return ItemStack.EMPTY;
        }

        RecipeEntry<Recipe<? extends RecipeInput>> recipe = recipePower.get().getRecipe();
        Identifier recipeId = recipe.id();

        if (!(recipe.value() instanceof CraftingRecipe craftingRecipe)) {
            return ItemStack.EMPTY;
        }

        ItemStack newResultStack = craftingRecipe.craft(input, lookup);
        Optional<ModifyCraftingPower> modifyCraftingPower = PowerHolderComponent.getPowers(playerEntity, ModifyCraftingPower.class)
            .stream()
            .filter(mcp -> mcp.doesApply(recipeId, newResultStack))
            .max(Comparator.comparing(ModifyCraftingPower::getPriority));

        if (modifyCraftingPower.isEmpty()) {
            return newResultStack;
        }

        pci.apoli$setPower(modifyCraftingPower.get());
        return modifyCraftingPower.get().getNewResult(InventoryUtil.createStackReference(newResultStack)).get();

    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private static List<RecipePower> getRecipePowers(CraftingRecipeInput input) {
        return input instanceof PowerCraftingInventory pci
            ? PowerHolderComponent.getPowers(pci.apoli$getPlayer(), RecipePower.class)
            : Lists.newArrayList();
    }

}
