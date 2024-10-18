package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import net.minecraft.entity.Entity;

import java.util.Objects;

public class EqualBiEntityConditionType extends BiEntityConditionType {

	@Override
	public boolean test(Entity actor, Entity target) {
		return Objects.equals(actor, target);
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return BiEntityConditionTypes.EQUAL;
	}

}
