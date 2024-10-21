package io.github.apace100.apoli.condition.type.entity.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.condition.type.meta.AnyOfMetaConditionType;
import net.minecraft.entity.Entity;

import java.util.List;

public class AnyOfEntityConditionType extends EntityConditionType implements AnyOfMetaConditionType<EntityConditionContext, EntityCondition> {

	private final List<EntityCondition> conditions;

	public AnyOfEntityConditionType(List<EntityCondition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public boolean test(Entity entity) {
		return testConditions(new EntityConditionContext(entity));
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return EntityConditionTypes.ANY_OF;
	}

	@Override
	public List<EntityCondition> conditions() {
		return conditions;
	}

}
