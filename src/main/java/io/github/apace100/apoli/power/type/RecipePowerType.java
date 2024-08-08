package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.RecipeInput;

public class RecipePowerType extends PowerType implements Prioritized<RecipePowerType> {

    private final RecipeEntry<Recipe<? extends RecipeInput>> recipe;
    private final int priority;

    public RecipePowerType(Power power, LivingEntity entity, RecipeEntry<Recipe<? extends RecipeInput>> recipe, int priority) {
        super(power, entity);
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

    public static PowerTypeFactory<RecipePowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("recipe"),
            new SerializableData()
                .add("recipe", SerializableDataTypes.RECIPE)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new RecipePowerType(power, entity,
                data.get("recipe"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
