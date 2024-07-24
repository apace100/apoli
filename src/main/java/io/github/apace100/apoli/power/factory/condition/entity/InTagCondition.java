package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.tag.TagKey;

public class InTagCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        TagKey<EntityType<?>> entityTypeTag = data.get("tag");
        return entity.getType().isIn(entityTypeTag);
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("in_tag"),
            new SerializableData()
                .add("tag", SerializableDataTypes.ENTITY_TAG),
            InTagCondition::condition
        );
    }

}
