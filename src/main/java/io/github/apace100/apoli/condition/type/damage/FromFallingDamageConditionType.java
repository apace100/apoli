package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import net.minecraft.registry.tag.DamageTypeTags;

@Deprecated(forRemoval = true)
public class FromFallingDamageConditionType extends InTagDamageConditionType {

	public FromFallingDamageConditionType() {
		super(DamageTypeTags.IS_FALL);
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return DamageConditionTypes.FROM_FALLING;
	}

}
