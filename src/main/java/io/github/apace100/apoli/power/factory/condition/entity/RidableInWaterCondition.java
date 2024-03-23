package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.Entity;

public class RidableInWaterCondition {
    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return !entity.shouldDismountUnderwater();
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("ridable_in_water"),
            new SerializableData(),
            RidableInWaterCondition::condition
        );
    }
}
