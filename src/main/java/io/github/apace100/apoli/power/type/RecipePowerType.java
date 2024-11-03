package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.mixin.RecipeManagerAccessor;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.recipe.PowerCraftingRecipe;
import io.github.apace100.calio.CalioServer;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.DataPackContents;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RecipePowerType extends PowerType implements Prioritized<RecipePowerType> {

    public static final TypedDataObjectFactory<RecipePowerType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("recipe", ApoliDataTypes.DISALLOWING_INTERNAL_CRAFTING_RECIPE)
            .add("priority", SerializableDataTypes.INT, 0),
        data -> new RecipePowerType(
            data.get("recipe"),
            data.get("priority")
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("recipe", powerType.getRecipe())
            .set("priority", powerType.getPriority())
    );

    private final CraftingRecipe recipe;
    private final int priority;

    public RecipePowerType(CraftingRecipe recipe, int priority) {
        this.recipe = recipe;
        this.priority = priority;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.RECIPE;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public CraftingRecipe getRecipe() {
        return recipe;
    }

    public static void registerPowerRecipes() {

        RecipeManager recipeManager = CalioServer.getDataPackContents()
            .map(DataPackContents::getRecipeManager)
            .orElse(null);

        if (recipeManager != null) {

            Map<Identifier, RecipeEntry<?>> recipeEntriesById = new Object2ObjectOpenHashMap<>(((RecipeManagerAccessor) recipeManager).getRecipesById());
            for (Power power : PowerManager.values()) {

                if (!(power.getPowerType() instanceof RecipePowerType recipePowerType)) {
                    continue;
                }

                Identifier powerId = power.getId();
                CraftingRecipe craftingRecipe = recipePowerType.getRecipe();

                //  Only register the power recipe if no other recipes have the same ID
                if (!recipeEntriesById.containsKey(powerId) || recipePowerType.getPriority() > 0) {
                    recipeEntriesById.put(powerId, new RecipeEntry<>(powerId, new PowerCraftingRecipe(powerId, craftingRecipe)));
                }

            }

            recipeManager.setRecipes(recipeEntriesById.values());

        }

    }

}
