package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;

public class RecipePowerType extends PowerType implements Prioritized<RecipePowerType> {

    private final RecipeEntry<CraftingRecipe> recipe;
    private final int priority;

    public RecipePowerType(Power power, LivingEntity entity, RecipeEntry<CraftingRecipe> recipe, int priority) {
        super(power, entity);
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

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("recipe"),
            new SerializableData()
                .add("recipe", ApoliDataTypes.CRAFTING_RECIPE)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new RecipePowerType(power, entity,
                data.get("recipe"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
