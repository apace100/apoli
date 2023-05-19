package io.github.apace100.apoli.util;

import com.google.common.collect.Lists;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.mixin.CraftingInventoryAccessor;
import io.github.apace100.apoli.mixin.CraftingScreenHandlerAccessor;
import io.github.apace100.apoli.mixin.PlayerScreenHandlerAccessor;
import io.github.apace100.apoli.power.RecipePower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PowerRestrictedCraftingRecipe extends SpecialCraftingRecipe {

    public static final RecipeSerializer<?> SERIALIZER = new SpecialRecipeSerializer<PowerRestrictedCraftingRecipe>(PowerRestrictedCraftingRecipe::new);

    public PowerRestrictedCraftingRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        return getRecipes(inv).stream().anyMatch(r -> r.matches(inv, world));
    }

    @Override
    public ItemStack craft(CraftingInventory inv, DynamicRegistryManager registryManager) {
        PlayerEntity player = getPlayerFromInventory(inv);
        if(player != null) {
            Optional<Recipe<CraftingInventory>> optional = getRecipes(inv).stream().filter(r -> r.matches(inv, player.world)).findFirst();
            if(optional.isPresent()) {
                Recipe<CraftingInventory> recipe = optional.get();
                return recipe.craft(inv, registryManager);
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

    private PlayerEntity getPlayerFromInventory(CraftingInventory inv) {
        ScreenHandler handler = ((CraftingInventoryAccessor)inv).getHandler();
        return getPlayerFromHandler(handler);
    }

    private List<Recipe<CraftingInventory>> getRecipes(CraftingInventory inv) {
        ScreenHandler handler = ((CraftingInventoryAccessor)inv).getHandler();
        PlayerEntity player = getPlayerFromHandler(handler);
        if(player != null) {
            return PowerHolderComponent.getPowers(player, RecipePower.class).stream().map(RecipePower::getRecipe).collect(Collectors.toList());
        }
        return Lists.newArrayList();
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
