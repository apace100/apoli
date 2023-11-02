package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.MovingEntity;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class ClimbingCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return entity instanceof LivingEntity livingEntity
            && (data.getBoolean("current") ? ((MovingEntity) livingEntity).apoli$activelyClimbing() : livingEntity.isClimbing());
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("climbing"),
            new SerializableData()
                .add("current", SerializableDataTypes.BOOLEAN, true),
            ClimbingCondition::condition
        );
    }

}
