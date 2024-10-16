package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.util.context.TypeConditionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.util.Validatable;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractConditionType<T extends TypeConditionContext, C extends AbstractCondition<T, ?>> implements Predicate<T>, Validatable {

	private Optional<C> condition = Optional.empty();

	@Override
	public abstract boolean test(T context);

	@SuppressWarnings("unchecked")
	@Override
	public void validate() throws Exception {

		DataObjectFactory<AbstractConditionType<T, C>> dataFactory = (DataObjectFactory<AbstractConditionType<T,C>>) configuration().dataFactory();
		SerializableData.Instance data = dataFactory.toData(this);

		data.validate();

	}

	public abstract ConditionConfiguration<?> configuration();

	public final Optional<C> getCondition() {
		return condition;
	}

	public void setCondition(Optional<C> condition) {
		this.condition = condition;
	}

}
