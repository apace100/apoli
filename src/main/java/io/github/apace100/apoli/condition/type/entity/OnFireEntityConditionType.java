package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import net.minecraft.entity.Entity;

public class OnFireEntityConditionType extends EntityConditionType {

	@Override
	public boolean test(Entity entity) {
		return entity.isOnFire();
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return EntityConditionTypes.ON_FIRE;
	}

}
