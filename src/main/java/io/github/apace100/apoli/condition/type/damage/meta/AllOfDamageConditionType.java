package io.github.apace100.apoli.condition.type.damage.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.DamageCondition;
import io.github.apace100.apoli.condition.context.DamageContext;
import io.github.apace100.apoli.condition.type.DamageConditionType;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import io.github.apace100.apoli.condition.type.meta.AllOfMetaConditionType;
import net.minecraft.entity.damage.DamageSource;

import java.util.List;

public class AllOfDamageConditionType extends DamageConditionType implements AllOfMetaConditionType<DamageContext, DamageCondition> {

	private final List<DamageCondition> conditions;

	public AllOfDamageConditionType(List<DamageCondition> conditions) {
		this.conditions = prepareConditions(this, conditions);
	}

	@Override
	public boolean test(DamageSource source, float amount) {
		return AllOfMetaConditionType.condition(new DamageContext(source, amount), conditions());
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return DamageConditionTypes.ALL_OF;
	}

	@Override
	public List<DamageCondition> conditions() {
		return conditions;
	}

}