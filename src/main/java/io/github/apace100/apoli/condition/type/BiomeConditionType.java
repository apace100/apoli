package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.BiomeCondition;
import io.github.apace100.apoli.condition.context.BiomeConditionContext;

public abstract class BiomeConditionType extends ConditionType<BiomeConditionContext, BiomeCondition> {

	@Override
	public BiomeCondition createCondition(boolean inverted) {
		return new BiomeCondition(this, inverted);
	}

}
