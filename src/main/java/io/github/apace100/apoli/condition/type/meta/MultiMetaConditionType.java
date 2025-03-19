package io.github.apace100.apoli.condition.type.meta;

import io.github.apace100.apoli.condition.Condition;
import io.github.apace100.apoli.condition.type.ConditionType;
import io.github.apace100.apoli.util.context.ConditionContext;

import java.util.List;

public interface MultiMetaConditionType<CX extends ConditionContext, CC extends Condition<CX, ? extends ConditionType<CX, CC>>> {

	List<CC> conditions();

}
