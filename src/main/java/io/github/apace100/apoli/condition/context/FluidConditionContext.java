package io.github.apace100.apoli.condition.context;

import io.github.apace100.apoli.util.context.TypeConditionContext;
import net.minecraft.fluid.FluidState;

public record FluidConditionContext(FluidState fluidState) implements TypeConditionContext {

}
