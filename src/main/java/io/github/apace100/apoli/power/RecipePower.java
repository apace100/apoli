package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.recipe.Recipe;

public class RecipePower extends Power {

    private final Recipe<CraftingInventory> recipe;

    public RecipePower(PowerType<?> type, LivingEntity entity, Recipe<CraftingInventory> recipe) {
        super(type, entity);
        this.recipe = recipe;
    }

    public Recipe<CraftingInventory> getRecipe() {
        return recipe;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("recipe"),
            new SerializableData()
                .add("recipe", SerializableDataTypes.RECIPE),
            data ->
                (type, player) -> {
                    Recipe<CraftingInventory> recipe = (Recipe<CraftingInventory>)data.get("recipe");
                    return new RecipePower(type, player, recipe);
                })
            .allowCondition();
    }
}
