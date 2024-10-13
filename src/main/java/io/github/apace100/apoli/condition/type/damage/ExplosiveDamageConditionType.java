package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import net.minecraft.registry.tag.DamageTypeTags;

@Deprecated(forRemoval = true)
public class ExplosiveDamageConditionType extends InTagDamageConditionType {

	public ExplosiveDamageConditionType() {
		super(DamageTypeTags.IS_EXPLOSION);
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return DamageConditionTypes.EXPLOSIVE;
	}

}
