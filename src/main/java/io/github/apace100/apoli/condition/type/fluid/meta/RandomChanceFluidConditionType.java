package io.github.apace100.apoli.condition.type.fluid.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.FluidConditionType;
import io.github.apace100.apoli.condition.type.FluidConditionTypes;
import io.github.apace100.apoli.condition.type.meta.RandomChanceMetaConditionType;
import net.minecraft.fluid.FluidState;

public class RandomChanceFluidConditionType extends FluidConditionType implements RandomChanceMetaConditionType {

	private final float chance;

	public RandomChanceFluidConditionType(float chance) {
		this.chance = chance;
	}

	@Override
	public boolean test(FluidState fluidState) {
		return testCondition();
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return FluidConditionTypes.RANDOM_CHANCE;
	}

	@Override
	public float chance() {
		return chance;
	}

}
