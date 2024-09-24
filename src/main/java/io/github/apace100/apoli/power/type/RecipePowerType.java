package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.mixin.RecipeManagerAccessor;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.recipe.PowerCraftingRecipe;
import io.github.apace100.calio.CalioServer;
import io.github.apace100.calio.data.SerializableData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.DataPackContents;
import net.minecraft.util.Identifier;

import java.util.Map;

public class RecipePowerType extends PowerType {

    private final CraftingRecipe recipe;

    public RecipePowerType(Power power, LivingEntity entity, CraftingRecipe recipe) {
        super(power, entity);
        this.recipe = recipe;
    }

    public CraftingRecipe getRecipe() {
        return recipe;
    }

    public static void registerPowerRecipes() {

        RecipeManager recipeManager = CalioServer.getDataPackContents()
            .map(DataPackContents::getRecipeManager)
            .orElse(null);

        if (recipeManager != null) {

            Map<Identifier, RecipeEntry<?>> recipes = new Object2ObjectOpenHashMap<>(((RecipeManagerAccessor) recipeManager).getRecipesById());
            for (Power power : PowerManager.values()) {

                if (power.getFactoryInstance().getFactory() != PowerTypes.RECIPE) {
                    continue;
                }

                Identifier powerId = power.getId();
                CraftingRecipe recipe = ((RecipePowerType) power.create(null)).getRecipe();

                //  Only register the power recipe if no other recipes have the same ID
                if (!recipes.containsKey(powerId)) {
                    recipes.put(powerId, new RecipeEntry<>(powerId, new PowerCraftingRecipe(powerId, recipe)));
                }

            }

            recipeManager.setRecipes(recipes.values());

        }

    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("recipe"),
            new SerializableData()
                .add("recipe", ApoliDataTypes.DISALLOWING_POWER_CRAFTING_RECIPE),
            data -> (power, entity) -> new RecipePowerType(power, entity,
                data.get("recipe")
            )
        ).allowCondition();
    }

}
