package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.function.Predicate;

public class InBlockAnywhereConditionType {

    public static boolean condition(Entity entity, Predicate<CachedBlockPosition> blockCondition, Comparison comparison, int compareTo) {

        Box boundingBox = entity.getBoundingBox();
        int countThreshold = switch (comparison) {
            case EQUAL, LESS_THAN_OR_EQUAL, GREATER_THAN ->
                compareTo + 1;
            case LESS_THAN, GREATER_THAN_OR_EQUAL ->
                compareTo;
            default ->
                -1;
        };

        BlockPos minPos = BlockPos.ofFloored(boundingBox.minX + 0.001D, boundingBox.minY + 0.001D, boundingBox.minZ + 0.001D);
        BlockPos maxPos = BlockPos.ofFloored(boundingBox.maxX - 0.001D, boundingBox.maxY - 0.001D, boundingBox.maxZ - 0.001D);

        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        int count = 0;

        for (int x = minPos.getX(); x <= maxPos.getX() && count < countThreshold; x++) {
            for (int y = minPos.getY(); y <= maxPos.getY() && count < countThreshold; y++) {
                for (int z = minPos.getZ(); z <= maxPos.getZ() && count < countThreshold; z++) {

                    mutablePos.set(x, y, z);

                    if (blockCondition.test(new CachedBlockPosition(entity.getWorld(), mutablePos, true))) {
                        count++;
                    }

                }
            }
        }

        return comparison.compare(count, compareTo);

    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("in_block_anywhere"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION)
                .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
                .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> condition(entity,
                data.get("block_condition"),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
