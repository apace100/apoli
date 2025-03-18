package io.github.apace100.apoli.condition.type.meta;

import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.type.AbstractConditionType;
import io.github.apace100.apoli.util.context.ConditionContext;

import java.util.List;

public interface MultiMetaConditionType<CX extends ConditionContext, CC extends AbstractCondition<CX, ? extends AbstractConditionType<CX, CC>>> {

	List<CC> conditions();

}
