package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Tameable;

public class TamedEntityConditionType extends EntityConditionType {

	@Override
	public boolean test(Entity entity) {
		return entity instanceof Tameable tameable
			&& tameable.getOwnerUuid() != null;
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return EntityConditionTypes.TAMED;
	}

}