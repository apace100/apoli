package io.github.apace100.apoli.condition.type.bientity.meta;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.condition.type.meta.AnyOfMetaConditionType;
import net.minecraft.entity.Entity;

import java.util.List;

public class AnyOfBiEntityConditionType extends BiEntityConditionType implements AnyOfMetaConditionType<BiEntityConditionContext, BiEntityCondition> {

	private final List<BiEntityCondition> conditions;

	public AnyOfBiEntityConditionType(List<BiEntityCondition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return BiEntityConditionTypes.ANY_OF;
	}

	@Override
	public boolean test(Entity actor, Entity target) {
		return testConditions(new BiEntityConditionContext(actor, target));
	}

	@Override
	public List<BiEntityCondition> conditions() {
		return conditions;
	}

}
