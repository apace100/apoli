package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class FoodLevelConditionType {

    public static boolean condition(Entity entity, Comparison comparison, int compareTo) {
        return entity instanceof PlayerEntity player
            && comparison.compare(player.getHungerManager().getFoodLevel(), compareTo);
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("food_level"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> condition(entity,
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
