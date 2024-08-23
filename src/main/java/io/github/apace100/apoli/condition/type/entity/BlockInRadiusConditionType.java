package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class BlockInRadiusConditionType {

    public static boolean condition(Entity entity, Predicate<CachedBlockPosition> blockCondition, int radius, Shape shape, Comparison comparison, int compareTo) {

        int countThreshold = switch (comparison) {
            case EQUAL, LESS_THAN_OR_EQUAL, GREATER_THAN ->
                compareTo + 1;
            case LESS_THAN, GREATER_THAN_OR_EQUAL ->
                compareTo;
            default ->
                -1;
        };

        int count = 0;
        for (BlockPos pos : Shape.getPositions(entity.getBlockPos(), shape, radius)) {

            if (blockCondition.test(new CachedBlockPosition(entity.getWorld(), pos, true))) {
                ++count;
            }

            if (count == countThreshold){
                break;
            }

        }

        return comparison.compare(count, compareTo);

    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("block_in_radius"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION)
                .add("radius", SerializableDataTypes.INT)
                .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
                .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
                .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> condition(entity,
                data.get("block_condition"),
                data.get("radius"),
                data.get("shape"),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
