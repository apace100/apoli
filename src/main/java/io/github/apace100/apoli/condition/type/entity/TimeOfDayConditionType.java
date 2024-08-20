package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class TimeOfDayConditionType {

    public static boolean condition(World world, Comparison comparison, int compareTo) {
        return comparison.compare(world.getTimeOfDay() % 24000L, compareTo);
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("time_of_day"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> condition(entity.getWorld(),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
