package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.action.AbstractAction;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.context.TypeActionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.Validatable;

import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractActionType<T extends TypeActionContext<?>, A extends AbstractAction<T, ?>> implements Consumer<T>, Validatable {

	private Optional<A> action = Optional.empty();

	@Override
	public abstract void accept(T context);

	@SuppressWarnings("unchecked")
	@Override
	public void validate() throws Exception {

		TypedDataObjectFactory<AbstractActionType<T, A>> dataFactory = (TypedDataObjectFactory<AbstractActionType<T,A>>) configuration().dataFactory();
		SerializableData.Instance data = dataFactory.toData(this);

		data.validate();

	}

	public abstract ActionConfiguration<?> configuration();

	public final Optional<A> getAction() {
		return action;
	}

	public void setAction(Optional<A> action) {
		this.action = action;
	}

}
