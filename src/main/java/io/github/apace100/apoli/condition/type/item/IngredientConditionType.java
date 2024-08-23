package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class IngredientConditionType {

    public static boolean condition(ItemStack stack, Ingredient ingredient) {
        return ingredient.test(stack);
    }

    public static ConditionTypeFactory<Pair<World, ItemStack>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("ingredient"),
            new SerializableData()
                .add("ingredient", SerializableDataTypes.INGREDIENT),
            (data, worldAndStack) -> condition(worldAndStack.getRight(),
                data.get("ingredient")
            )
        );
    }

}
