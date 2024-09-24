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

    private final RecipeEntry<CraftingRecipe> recipeEntry;

    public RecipePowerType(Power power, LivingEntity entity, CraftingRecipe recipe) {
        super(power, entity);
        this.recipeEntry = new RecipeEntry<>(power.getId(), new PowerCraftingRecipe(power.getId(), recipe));
    }

    public RecipeEntry<CraftingRecipe> getRecipeEntry() {
        return recipeEntry;
    }

    public static void registerPowerRecipes() {

        RecipeManager recipeManager = CalioServer.getDataPackContents()
            .map(DataPackContents::getRecipeManager)
            .orElse(null);

        if (recipeManager != null) {

            Map<Identifier, RecipeEntry<?>> recipeEntriesById = new Object2ObjectOpenHashMap<>(((RecipeManagerAccessor) recipeManager).getRecipesById());
            for (Power power : PowerManager.values()) {

                if (!(power.create(null) instanceof RecipePowerType recipePowerType)) {
                    continue;
                }

                Identifier powerId = power.getId();
                RecipeEntry<CraftingRecipe> recipeEntry = recipePowerType.getRecipeEntry();

                //  Only register the power recipe if no other recipes have the same ID
                if (!recipeEntriesById.containsKey(powerId)) {
                    recipeEntriesById.put(powerId, recipeEntry);
                }

            }

            recipeManager.setRecipes(recipeEntriesById.values());

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
