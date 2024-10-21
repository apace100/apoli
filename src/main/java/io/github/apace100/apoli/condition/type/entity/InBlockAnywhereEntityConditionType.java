package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class InBlockAnywhereEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<InBlockAnywhereEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("block_condition", BlockCondition.DATA_TYPE)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
            .add("compare_to", SerializableDataTypes.INT, 0),
        data -> new InBlockAnywhereEntityConditionType(
            data.get("block_condition"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("block_condition", conditionType.blockCondition)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final BlockCondition blockCondition;
    private final Comparison comparison;

    private final int compareTo;
    private final int threshold;

    public InBlockAnywhereEntityConditionType(BlockCondition blockCondition, Comparison comparison, int compareTo) {
        this.blockCondition = blockCondition;
        this.comparison = comparison;
        this.compareTo = compareTo;
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

        Box boundingBox = entity.getBoundingBox();
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        BlockPos minPos = BlockPos.ofFloored(boundingBox.minX + 0.001D, boundingBox.minY + 0.001D, boundingBox.minZ + 0.001D);
        BlockPos maxPos = BlockPos.ofFloored(boundingBox.maxX - 0.001D, boundingBox.maxY - 0.001D, boundingBox.maxZ - 0.001D);

        int matches = 0;
        for (int x = minPos.getX(); x <= maxPos.getX() && matches < threshold; x++) {
            for (int y = minPos.getY(); y <= maxPos.getY() && matches < threshold; y++) {
                for (int z = minPos.getZ(); z <= maxPos.getZ() && matches < threshold; z++) {

                    mutablePos.set(x, y, z);

                    if (blockCondition.test(entity.getWorld(), mutablePos)) {
                        ++matches;
                    }

                }
            }
        }

        return comparison.compare(matches, compareTo);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.IN_BLOCK_ANYWHERE;
    }

}
