package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public class EntityTypeConditionType {

    public static boolean condition(Entity entity, EntityType<?> entityType) {
        return entity.getType().equals(entityType);
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("entity_type"),
            new SerializableData()
                .add("entity_type", SerializableDataTypes.ENTITY_TYPE),
            (data, entity) -> condition(entity,
                data.get("entity_type")
            )
        );
    }

}
