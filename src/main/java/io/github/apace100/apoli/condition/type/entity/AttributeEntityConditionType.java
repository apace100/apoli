package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Optional;

public class AttributeEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<AttributeEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("attribute", SerializableDataTypes.ATTRIBUTE_ENTRY)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.DOUBLE),
        data -> new AttributeEntityConditionType(
            data.get("attribute"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("attribute", conditionType.attribute)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final RegistryEntry<EntityAttribute> attribute;

    private final Comparison comparison;
    private final double compareTo;

    public AttributeEntityConditionType(RegistryEntry<EntityAttribute> attribute, Comparison comparison, double compareTo) {
        this.attribute = attribute;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(Entity entity) {

        if (entity instanceof LivingEntity livingEntity) {
            return Optional.ofNullable(livingEntity.getAttributeInstance(attribute))
                .map(EntityAttributeInstance::getValue)
                .map(value -> comparison.compare(value, compareTo))
                .orElse(false);
        }

        else {
            return false;
        }

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.ATTRIBUTE;
    }

}
