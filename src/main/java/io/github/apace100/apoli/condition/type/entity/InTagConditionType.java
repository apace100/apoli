package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.tag.TagKey;

public class InTagConditionType {

    public static boolean condition(Entity entity, TagKey<EntityType<?>> entityTag) {
        return entity.getType().isIn(entityTag);
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("in_tag"),
            new SerializableData()
                .add("tag", SerializableDataTypes.ENTITY_TAG),
            (data, entity) -> condition(entity,
                data.get("tag")
            )
        );
    }

}
