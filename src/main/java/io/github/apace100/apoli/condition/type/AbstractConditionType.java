package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.util.Validatable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractConditionType<T, C extends AbstractCondition<T, ?>> implements Predicate<T>, Validatable {

	private C condition = null;

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

	public final Optional<PowerType> getPowerType() {
		return getCondition().flatMap(AbstractCondition::getPowerType);
	}

	public final Optional<C> getCondition() {
		return Optional.ofNullable(condition);
	}

	public final void setCondition(@NotNull C condition) {
		this.condition = condition;
	}

}
