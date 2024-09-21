package io.github.apace100.apoli.condition.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.math.random.Random;

public class RandomChanceConditionType {

    public static boolean condition(float chance) {
        return Random.create().nextFloat() < chance;
    }

    public static <T> ConditionTypeFactory<T> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("random_chance"),
            new SerializableData()
                .add("chance", SerializableDataTypes.FLOAT),
            (data, type) -> condition(
                data.get("chance")
            )
        );
    }

}
