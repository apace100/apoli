package io.github.apace100.apoli.condition.type.entity.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.condition.type.meta.ConstantMetaConditionType;
import net.minecraft.entity.Entity;

public class ConstantEntityConditionType extends EntityConditionType implements ConstantMetaConditionType {

	private final boolean value;

	public ConstantEntityConditionType(boolean value) {
		this.value = value;
	}

	@Override
	public boolean test(Entity entity) {
		return value();
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return EntityConditionTypes.CONSTANT;
	}

	@Override
	public boolean value() {
		return value;
	}

}
