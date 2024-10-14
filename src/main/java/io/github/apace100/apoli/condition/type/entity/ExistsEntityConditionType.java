package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import net.minecraft.entity.Entity;

public class ExistsEntityConditionType extends EntityConditionType {

	@Override
	public boolean test(Entity entity) {
		return entity != null;
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return EntityConditionTypes.EXISTS;
	}

}
