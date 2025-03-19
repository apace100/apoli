package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import io.github.apace100.apoli.util.requirement.BiEntityRequirement;

public abstract class BiEntityConditionType extends ConditionType<BiEntityConditionContext, BiEntityCondition> {

	@Override
	public BiEntityCondition createCondition(boolean inverted) {
		return new BiEntityCondition(this, inverted);
	}

	@Override
	public boolean shouldTest(BiEntityConditionContext context) {
		return switch (getRequirement()) {
			case BOTH ->
				context.actor() != null && context.target() != null;
			case EITHER ->
				context.actor() != null || context.target() != null;
			case DEFAULT ->
				super.shouldTest(context);
		};
	}

	public BiEntityRequirement getRequirement() {
		return BiEntityRequirement.DEFAULT;
	}

}
