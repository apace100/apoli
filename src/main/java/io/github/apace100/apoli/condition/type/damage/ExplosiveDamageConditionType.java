package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import org.jetbrains.annotations.NotNull;

@Deprecated(forRemoval = true)
public class ExplosiveDamageConditionType extends InTagDamageConditionType {

	public ExplosiveDamageConditionType() {
		super(DamageTypeTags.IS_EXPLOSION);
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return DamageConditionTypes.EXPLOSIVE;
	}

}
