package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public class EntityTypeEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<EntityTypeEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("entity_type", SerializableDataTypes.ENTITY_TYPE),
        data -> new EntityTypeEntityConditionType(
            data.get("entity_type")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("entity_type", conditionType.entityType)
    );

    private final EntityType<?> entityType;

    public EntityTypeEntityConditionType(EntityType<?> entityType) {
        this.entityType = entityType;
    }

    @Override
    public boolean test(Entity entity) {
        return entity.getType().equals(entityType);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.ENTITY_TYPE;
    }

}
