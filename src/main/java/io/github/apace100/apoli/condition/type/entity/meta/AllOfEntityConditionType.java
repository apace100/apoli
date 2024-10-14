package io.github.apace100.apoli.condition.type.entity.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.context.EntityContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.condition.type.meta.AllOfMetaConditionType;
import net.minecraft.entity.Entity;

import java.util.List;

public class AllOfEntityConditionType extends EntityConditionType implements AllOfMetaConditionType<EntityContext, EntityCondition> {

	private final List<EntityCondition> conditions;

	public AllOfEntityConditionType(List<EntityCondition> conditions) {
		this.conditions = prepareConditions(this, conditions);
	}

	@Override
	public boolean test(Entity entity) {
		return AllOfMetaConditionType.condition(new EntityContext(entity), conditions());
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return EntityConditionTypes.ALL_OF;
	}

	@Override
	public List<EntityCondition> conditions() {
		return conditions;
	}

}