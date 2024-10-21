package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.util.Comparison;

public class DayTimeEntityConditionType extends TimeOfDayEntityConditionType {

	public DayTimeEntityConditionType() {
		super(Comparison.LESS_THAN, 13000);
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return EntityConditionTypes.DAY_TIME;
	}

}
