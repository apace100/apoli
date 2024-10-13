package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.World;

public class IngredientItemConditionType extends ItemConditionType {

    public static final DataObjectFactory<IngredientItemConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("ingredient", SerializableDataTypes.INGREDIENT),
        data -> new IngredientItemConditionType(
            data.get("ingredient")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("ingredient", conditionType.ingredient)
    );

    private final Ingredient ingredient;

    public IngredientItemConditionType(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public boolean test(World world, ItemStack stack) {
        return ingredient.test(stack);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return ItemConditionTypes.INGREDIENT;
    }

}
