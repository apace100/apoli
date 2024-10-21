package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import net.minecraft.registry.tag.DamageTypeTags;

public class UnblockableDamageConditionType extends InTagDamageConditionType {

	public UnblockableDamageConditionType() {
		super(DamageTypeTags.BYPASSES_SHIELD);
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return DamageConditionTypes.UNBLOCKABLE;
	}

}
