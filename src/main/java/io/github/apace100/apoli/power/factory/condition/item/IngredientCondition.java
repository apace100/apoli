package io.github.apace100.apoli.power.factory.condition.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class IngredientCondition {

    public static boolean condition(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {
        return data.<Ingredient>get("ingredient").test(worldAndStack.getRight());
    }

    public static ConditionFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("ingredient"),
            new SerializableData()
                .add("ingredient", SerializableDataTypes.INGREDIENT),
            IngredientCondition::condition
        );
    }

}
