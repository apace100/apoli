package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SlipperinessBlockConditionType extends BlockConditionType {

    public static final TypedDataObjectFactory<SlipperinessBlockConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
        data -> new SlipperinessBlockConditionType(
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final Comparison comparison;
    private final float compareTo;

    public SlipperinessBlockConditionType(Comparison comparison, float compareTo) {
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(World world, BlockPos pos) {
        return comparison.compare(world.getBlockState(pos).getBlock().getSlipperiness(), compareTo);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BlockConditionTypes.SLIPPERINESS;
    }

}
