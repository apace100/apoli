package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.BlockCondition;
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
import net.minecraft.util.math.BlockPos;

public class BlockInRadiusEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<BlockInRadiusEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("block_condition", BlockCondition.DATA_TYPE)
            .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
            .add("compare_to", SerializableDataTypes.INT, 0)
            .add("radius", SerializableDataTypes.INT),
        data -> new BlockInRadiusEntityConditionType(
            data.get("block_condition"),
            data.get("shape"),
            data.get("comparison"),
            data.get("compare_to"),
            data.get("radius")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("block_condition", conditionType.blockCondition)
            .set("shape", conditionType.shape)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
            .set("radius", conditionType.radius)
    );

    private final BlockCondition blockCondition;
    private final Shape shape;

    private final Comparison comparison;
    private final int compareTo;

    private final int radius;
    private final int threshold;

    public BlockInRadiusEntityConditionType(BlockCondition blockCondition, Shape shape, Comparison comparison, int compareTo, int radius) {
        this.blockCondition = blockCondition;
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
        for (BlockPos pos : Shape.getPositions(entity.getBlockPos(), shape, radius)) {

            if (blockCondition.test(entity.getWorld(), pos)) {
                ++matches;
            }

            if (matches == threshold) {
                break;
            }

        }

        return comparison.compare(matches ,compareTo);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.BLOCK_IN_RADIUS;
    }

}
