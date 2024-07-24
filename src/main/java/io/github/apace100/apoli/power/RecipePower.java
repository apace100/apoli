package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.RecipeInput;

public class RecipePower extends Power implements Prioritized<RecipePower> {

    private final RecipeEntry<Recipe<? extends RecipeInput>> recipe;
    private final int priority;

    public RecipePower(PowerType<?> type, LivingEntity entity, RecipeEntry<Recipe<? extends RecipeInput>> recipe, int priority) {
        super(type, entity);
        this.recipe = recipe;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public RecipeEntry<Recipe<? extends RecipeInput>> getRecipe() {
        return recipe;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("recipe"),
            new SerializableData()
                .add("recipe", SerializableDataTypes.RECIPE)
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
