package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import net.minecraft.registry.tag.DamageTypeTags;

@Deprecated(forRemoval = true)
public class FireDamageConditionType extends InTagDamageConditionType {

	public FireDamageConditionType() {
		super(DamageTypeTags.IS_FIRE);
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return DamageConditionTypes.FIRE;
	}

}
