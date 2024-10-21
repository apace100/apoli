package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.FluidCondition;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FluidBlockConditionType extends BlockConditionType {

    public static final TypedDataObjectFactory<FluidBlockConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("fluid_condition", FluidCondition.DATA_TYPE),
        data -> new FluidBlockConditionType(
            data.get("fluid_condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("fluid_condition", conditionType.fluidCondition)
    );

    private final FluidCondition fluidCondition;

    public FluidBlockConditionType(FluidCondition fluidCondition) {
        this.fluidCondition = fluidCondition;
    }

    @Override
    public boolean test(World world, BlockPos pos) {
        return fluidCondition.test(world.getFluidState(pos));
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BlockConditionTypes.FLUID;
    }

}
