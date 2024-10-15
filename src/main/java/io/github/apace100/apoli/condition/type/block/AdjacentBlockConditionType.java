package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class AdjacentBlockConditionType extends BlockConditionType {

    public static final DataObjectFactory<AdjacentBlockConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("adjacent_condition", BlockCondition.DATA_TYPE)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
        data -> new AdjacentBlockConditionType(
            data.get("adjacent_condition"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("adjacent_condition", conditionType.adjacentCondition)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final BlockCondition adjacentCondition;
    private final Comparison comparison;

    private final int compareTo;

    public AdjacentBlockConditionType(BlockCondition adjacentCondition, Comparison comparison, int compareTo) {
        this.adjacentCondition = adjacentCondition;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(World world, BlockPos pos) {

        int matches = 0;
        for (Direction direction : Direction.values()) {

            if (adjacentCondition.test(world, pos.offset(direction))) {
                matches++;
            }

        }

        return comparison.compare(matches, compareTo);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BlockConditionTypes.ADJACENT;
    }

}
