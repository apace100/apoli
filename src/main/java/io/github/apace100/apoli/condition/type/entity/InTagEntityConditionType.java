package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.tag.TagKey;

public class InTagEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<InTagEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("tag", SerializableDataTypes.ENTITY_TAG),
        data -> new InTagEntityConditionType(
            data.get("tag")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("tag", conditionType.tag)
    );

    private final TagKey<EntityType<?>> tag;

    public InTagEntityConditionType(TagKey<EntityType<?>> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(Entity entity) {
        return entity.getType().isIn(tag);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.IN_TAG;
    }

}
