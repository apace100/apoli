package io.github.apace100.apoli.condition.type.entity.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.condition.type.meta.RandomChanceMetaConditionType;
import net.minecraft.entity.Entity;

public class RandomChanceEntityConditionType extends EntityConditionType implements RandomChanceMetaConditionType {

	private final float chance;

	public RandomChanceEntityConditionType(float chance) {
		this.chance = chance;
	}

	@Override
	public boolean test(Entity entity) {
		return testCondition();
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return EntityConditionTypes.RANDOM_CHANCE;
	}

	@Override
	public float chance() {
		return chance;
	}

}
