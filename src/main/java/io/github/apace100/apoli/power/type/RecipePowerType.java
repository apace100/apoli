package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class RecipePowerType extends PowerType {

    private final Identifier recipe;

    public RecipePowerType(Power power, LivingEntity entity, Identifier recipe) {
        super(power, entity);
        this.recipe = recipe;
    }

    public Identifier getRecipeId() {
        return recipe;
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("recipe"),
            new SerializableData()
                .add("recipe", ApoliDataTypes.POWER_CRAFTING_RECIPE_ID),
            data -> (power, entity) -> new RecipePowerType(power, entity,
                data.get("recipe")
            )
        ).allowCondition();
    }

}
