package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;

public class EntityInRadiusEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<EntityInRadiusEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("bientity_condition", BiEntityCondition.DATA_TYPE)
            .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
            .add("compare_to", SerializableDataTypes.INT, 0)
            .add("radius", SerializableDataTypes.DOUBLE),
        data -> new EntityInRadiusEntityConditionType(
            data.get("bientity_condition"),
            data.get("shape"),
            data.get("comparison"),
            data.get("compare_to"),
            data.get("radius")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("bientity_condition", conditionType.biEntityCondition)
            .set("shape", conditionType.shape)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
            .set("radius", conditionType.radius)
    );

    private final BiEntityCondition biEntityCondition;
    private final Shape shape;

    private final Comparison comparison;
    private final int compareTo;

    private final double radius;
    private final int threshold;

    public EntityInRadiusEntityConditionType(BiEntityCondition biEntityCondition, Shape shape, Comparison comparison, int compareTo, double radius) {
        this.biEntityCondition = biEntityCondition;
        this.shape = shape;
        this.comparison = comparison;
        this.compareTo = compareTo;
        this.radius = radius;
        this.threshold = switch (comparison) {
            case EQUAL, LESS_THAN_OR_EQUAL, GREATER_THAN ->
                compareTo + 1;
            case LESS_THAN, GREATER_THAN_OR_EQUAL ->
                compareTo;
            default ->
                -1;
        };
    }

    @Override
    public boolean test(Entity entity) {

        int matches = 0;
        for (Entity target : Shape.getEntities(shape, entity.getWorld(), entity.getLerpedPos(1.0F), radius)) {

            if (biEntityCondition.test(entity, target)) {
                ++matches;
            }

            if (matches == threshold) {
                break;
            }

        }

        return comparison.compare(matches, compareTo);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.ENTITY_IN_RADIUS;
    }

}
