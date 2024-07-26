package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;

public class RecipePower extends Power implements Prioritized<RecipePower> {

    private final RecipeEntry<CraftingRecipe> recipe;
    private final int priority;

    public RecipePower(PowerType<?> type, LivingEntity entity, RecipeEntry<CraftingRecipe> recipe, int priority) {
        super(type, entity);
        this.recipe = recipe;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public RecipeEntry<CraftingRecipe> getRecipe() {
        return recipe;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("recipe"),
            new SerializableData()
                .add("recipe", ApoliDataTypes.CRAFTING_RECIPE)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (powerType, livingEntity) -> new RecipePower(
                powerType,
                livingEntity,
                data.get("recipe"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
