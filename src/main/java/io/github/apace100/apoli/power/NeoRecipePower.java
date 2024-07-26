package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class NeoRecipePower extends Power {

    private final Identifier recipeId;

    public NeoRecipePower(PowerType<?> type, LivingEntity entity, Identifier recipeId) {
        super(type, entity);
        this.recipeId = recipeId;
    }

    public Identifier getRecipeId() {
        return recipeId;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("neo_recipe"),
            new SerializableData()
                .add("recipe", ApoliDataTypes.POWER_CRAFTING_RECIPE_ID),
            data -> (powerType, livingEntity) -> new NeoRecipePower(
                powerType,
                livingEntity,
                data.getId("recipe")
            )
        ).allowCondition();
    }

}
