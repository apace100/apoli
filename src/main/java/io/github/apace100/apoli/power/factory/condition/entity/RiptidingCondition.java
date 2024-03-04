package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;

public class RiptidingCondition {
    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return (entity instanceof LivingEntity) ? ((LivingEntity) entity).isUsingRiptide() : false;
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("riptiding"),
            new SerializableData(),
            RiptidingCondition::condition
        );
    }
}
