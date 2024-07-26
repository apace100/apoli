package io.github.apace100.apoli.util;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyCraftingPower;
import io.github.apace100.apoli.power.RecipePower;
import io.github.apace100.apoli.recipe.ApoliRecipeSerializers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.apache.commons.compress.utils.Lists;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

//  TODO: Either remove this completely, or implement a backwards compatibility layer that enables users to either this if a crafting recipe is
//        defined, or use the new one if a power recipe ID is defined in a power -eggohito
@Deprecated
public class LegacyPowerCraftingRecipe extends SpecialCraftingRecipe {

    public LegacyPowerCraftingRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        return getRecipePowers(input)
            .stream()
            .map(RecipePower::getRecipe)
            .map(RecipeEntry::value)
            .anyMatch(recipe -> recipe.matches(input, world));
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {

        if (!(input instanceof PowerCraftingInventory pci) || pci.apoli$getPlayer() == null) {
            return ItemStack.EMPTY;
        }

        PlayerEntity player = pci.apoli$getPlayer();
        Optional<RecipeEntry<CraftingRecipe>> matchingRecipe = getRecipePowers(input)
            .stream()
            .filter(power -> power.getRecipe().value().matches(input, player.getWorld()))
            .max(Comparator.comparing(RecipePower::getPriority))
            .map(RecipePower::getRecipe);

        if (matchingRecipe.isEmpty()) {
            return ItemStack.EMPTY;
        }

        RecipeEntry<CraftingRecipe> recipe = matchingRecipe.get();
        Identifier recipeId = recipe.id();

        ItemStack newResultStack = recipe.value().craft(input, lookup);
        Optional<ModifyCraftingPower> modifyCraftingPower = PowerHolderComponent.getPowers(player, ModifyCraftingPower.class)
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
        return ApoliRecipeSerializers.LEGACY_POWER_CRAFTING;
    }

    private static List<RecipePower> getRecipePowers(CraftingRecipeInput input) {
        return input instanceof PowerCraftingInventory pci
            ? PowerHolderComponent.getPowers(pci.apoli$getPlayer(), RecipePower.class)
            : Lists.newArrayList();
    }

}
