package io.github.apace100.apoli.condition.type.bientity.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.condition.type.meta.RandomChanceMetaConditionType;
import net.minecraft.entity.Entity;

public class RandomChanceBiEntityConditionType extends BiEntityConditionType implements RandomChanceMetaConditionType {

	private final float chance;

	public RandomChanceBiEntityConditionType(float chance) {
		this.chance = chance;
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return BiEntityConditionTypes.RANDOM_CHANCE;
	}

	@Override
	public boolean test(Entity actor, Entity target) {
		return testCondition();
	}

	@Override
	public float chance() {
		return chance;
	}

}
