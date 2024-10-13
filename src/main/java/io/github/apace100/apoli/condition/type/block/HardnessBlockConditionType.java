package io.github.apace100.apoli.condition.type.block;

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
import net.minecraft.world.World;

public class HardnessBlockConditionType extends BlockConditionType {

    public static final DataObjectFactory<HardnessBlockConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
        data -> new HardnessBlockConditionType(
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final Comparison comparison;
    private final float compareTo;

    public HardnessBlockConditionType(Comparison comparison, float compareTo) {
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(World world, BlockPos pos) {
        return comparison.compare(world.getBlockState(pos).getHardness(world, pos), compareTo);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BlockConditionTypes.HARDNESS;
    }

}
