package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.DamageCondition;
import io.github.apace100.apoli.condition.context.DamageContext;
import net.minecraft.entity.damage.DamageSource;

public abstract class DamageConditionType extends AbstractConditionType<DamageContext, DamageCondition> {

	@Override
	public final boolean test(DamageContext context) {
		return test(context.source(), context.amount());
	}

	public abstract boolean test(DamageSource source, float amount);

}
