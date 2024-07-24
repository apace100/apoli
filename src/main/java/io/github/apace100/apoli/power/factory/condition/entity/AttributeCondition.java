package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;

public class AttributeCondition {

    public static boolean condition(Entity entity, RegistryEntry<EntityAttribute> attributeEntry, Comparison comparison, double compareTo) {

        double attributeValue = 0.0D;

        if (entity instanceof LivingEntity livingEntity) {

            EntityAttributeInstance attributeInstance = livingEntity.getAttributeInstance(attributeEntry);

            if (attributeInstance != null) {
                attributeValue = attributeInstance.getValue();
            }

        }

        return comparison.compare(attributeValue, compareTo);

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("attribute"),
            new SerializableData()
                .add("attribute", SerializableDataTypes.ATTRIBUTE_ENTRY)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.DOUBLE),
            (data, entity) -> condition(
                entity,
                data.get("attribute"),
                data.get("comparison"),
                data.getDouble("compare_to")
            )
        );
    }

}
