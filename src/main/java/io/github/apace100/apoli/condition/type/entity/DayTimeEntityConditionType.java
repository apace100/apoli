package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.util.Comparison;
import org.jetbrains.annotations.NotNull;

public class DayTimeEntityConditionType extends TimeOfDayEntityConditionType {

	public DayTimeEntityConditionType() {
		super(Comparison.LESS_THAN, 13000);
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return EntityConditionTypes.DAY_TIME;
	}

}
