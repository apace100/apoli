package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class CreativeFlyingEntityConditionType extends EntityConditionType {

	@Override
	public boolean test(Entity entity) {
		return entity instanceof PlayerEntity player
			&& player.getAbilities().flying;
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return EntityConditionTypes.CREATIVE_FLYING;
	}

}
