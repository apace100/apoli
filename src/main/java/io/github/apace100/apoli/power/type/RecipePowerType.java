package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class RecipePowerType extends PowerType implements Prioritized<RecipePowerType> {

    private final Identifier recipe;
    private final int priority;

    public RecipePowerType(Power power, LivingEntity entity, Identifier recipe, int priority) {
        super(power, entity);
        this.recipe = recipe;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public Identifier getRecipeId() {
        return recipe;
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("recipe"),
            new SerializableData()
                .add("recipe", ApoliDataTypes.POWER_CRAFTING_RECIPE_ID)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new RecipePowerType(power, entity,
                data.get("recipe"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
