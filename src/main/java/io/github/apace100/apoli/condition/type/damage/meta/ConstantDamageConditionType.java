package io.github.apace100.apoli.condition.type.damage.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.DamageConditionType;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import io.github.apace100.apoli.condition.type.meta.ConstantMetaConditionType;
import net.minecraft.entity.damage.DamageSource;

public class ConstantDamageConditionType extends DamageConditionType implements ConstantMetaConditionType {

	private final boolean value;

	public ConstantDamageConditionType(boolean value) {
		this.value = value;
	}

	@Override
	public boolean test(DamageSource source, float amount) {
		return value();
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return DamageConditionTypes.CONSTANT;
	}

	@Override
	public boolean value() {
		return value;
	}

}
