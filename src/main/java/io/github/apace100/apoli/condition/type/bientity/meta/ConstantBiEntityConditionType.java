package io.github.apace100.apoli.condition.type.bientity.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.condition.type.meta.ConstantMetaConditionType;
import net.minecraft.entity.Entity;

public class ConstantBiEntityConditionType extends BiEntityConditionType implements ConstantMetaConditionType {

	private final boolean value;

	public ConstantBiEntityConditionType(boolean value) {
		this.value = value;
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return BiEntityConditionTypes.CONSTANT;
	}

	@Override
	public boolean test(Entity actor, Entity target) {
		return value();
	}

	@Override
	public boolean value() {
		return value;
	}

}
