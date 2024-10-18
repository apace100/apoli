package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import net.minecraft.registry.tag.DamageTypeTags;

@Deprecated(forRemoval = true)
public class BypassesArmorDamageConditionType extends InTagDamageConditionType {

	public BypassesArmorDamageConditionType() {
		super(DamageTypeTags.BYPASSES_ARMOR);
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return DamageConditionTypes.BYPASSES_ARMOR;
	}

}
