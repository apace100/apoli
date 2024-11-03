package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import org.jetbrains.annotations.NotNull;

@Deprecated(forRemoval = true)
public class OutOfWorldDamageConditionType extends InTagDamageConditionType {

	public OutOfWorldDamageConditionType() {
		super(DamageTypeTags.BYPASSES_INVULNERABILITY);
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return DamageConditionTypes.OUT_OF_WORLD;
	}

}
