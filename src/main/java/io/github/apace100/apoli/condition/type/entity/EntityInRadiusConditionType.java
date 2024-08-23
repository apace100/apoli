package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class EntityInRadiusConditionType {

    public static boolean condition(Entity entity, Predicate<Pair<Entity, Entity>> biEntityCondition, Shape shape, double radius, Comparison comparison, int compareTo) {

        int countThreshold = switch (comparison) {
            case EQUAL, LESS_THAN_OR_EQUAL, GREATER_THAN ->
                compareTo + 1;
            case LESS_THAN, GREATER_THAN_OR_EQUAL ->
                compareTo;
            default ->
                -1;
        };

        int count = 0;
        for (Entity target : Shape.getEntities(shape, entity.getWorld(), entity.getLerpedPos(1.0F), radius)) {

            if (biEntityCondition.test(new Pair<>(entity, target))) {
                ++count;
            }

            if (count == countThreshold) {
                break;
            }

        }

        return comparison.compare(count, compareTo);

    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("entity_in_radius"),
            new SerializableData()
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION)
                .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
                .add("radius", SerializableDataTypes.DOUBLE)
                .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
                .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> condition(entity,
                data.get("bientity_condition"),
                data.get("shape"),
                data.get("radius"),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
