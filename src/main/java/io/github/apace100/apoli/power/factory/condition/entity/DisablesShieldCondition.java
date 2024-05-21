package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.Entity;

public class DisablesShieldCondition {
    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return (entity instanceof MobEntity) ? ((MobEntity) entity).disablesShield() : false;
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("disables_shield"),
            new SerializableData(),
            DisablesShieldCondition::condition
        );
    }
}
