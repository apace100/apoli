package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.fluid.FluidState;

import java.util.function.Predicate;

public class FluidConditionType {

    public static boolean condition(CachedBlockPosition cachedBlock, Predicate<FluidState> fluidCondition) {
        return fluidCondition.test(cachedBlock.getWorld().getFluidState(cachedBlock.getBlockPos()));
    }

    public static ConditionTypeFactory<CachedBlockPosition> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("fluid"),
            new SerializableData()
                .add("fluid_condition", ApoliDataTypes.FLUID_CONDITION),
            (data, cachedBlock) -> condition(cachedBlock,
                data.get("fluid_condition")
            )
        );
    }

}
