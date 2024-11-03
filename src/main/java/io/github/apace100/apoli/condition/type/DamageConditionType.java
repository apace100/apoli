package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.DamageCondition;
import io.github.apace100.apoli.condition.context.DamageConditionContext;
import net.minecraft.entity.damage.DamageSource;

public abstract class DamageConditionType extends AbstractConditionType<DamageConditionContext, DamageCondition> {

	@Override
	public boolean test(DamageConditionContext context) {
		return test(context.source(), context.amount());
	}

	@Override
	public DamageCondition createCondition(boolean inverted) {
		return new DamageCondition(this, inverted);
	}

	public abstract boolean test(DamageSource source, float amount);

}
