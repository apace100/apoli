package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class ClimbingCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return entity instanceof LivingEntity livingEntity && livingEntity.isClimbing();
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("climbing"),
            new SerializableData(),
            ClimbingCondition::condition
        );
    }

}
