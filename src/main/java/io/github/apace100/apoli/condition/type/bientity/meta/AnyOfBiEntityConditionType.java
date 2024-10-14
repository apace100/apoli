package io.github.apace100.apoli.condition.type.bientity.meta;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BiEntityContext;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.condition.type.meta.AnyOfMetaConditionType;
import net.minecraft.entity.Entity;

import java.util.List;

public class AnyOfBiEntityConditionType extends BiEntityConditionType implements AnyOfMetaConditionType<BiEntityContext, BiEntityCondition> {

	private final List<BiEntityCondition> conditions;

	public AnyOfBiEntityConditionType(List<BiEntityCondition> conditions) {
		this.conditions = prepareConditions(this, conditions);
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return BiEntityConditionTypes.ANY_OF;
	}

	@Override
	public boolean test(Entity actor, Entity target) {
		return AnyOfMetaConditionType.condition(new BiEntityContext(actor, target), conditions());
	}

	@Override
	public List<BiEntityCondition> conditions() {
		return conditions;
	}

}