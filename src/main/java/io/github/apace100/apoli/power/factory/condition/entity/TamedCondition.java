package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Tameable;

public class TamedCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return entity instanceof Tameable tameable
            && tameable.getOwnerUuid() != null;
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("tamed"),
            new SerializableData(),
            TamedCondition::condition
        );
    }

}
