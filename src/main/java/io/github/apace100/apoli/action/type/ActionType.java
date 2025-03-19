package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.action.Action;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.context.ActionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.Validatable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class ActionType<AX extends ActionContext<?>, A extends Action<AX, ?>> implements Consumer<AX>, Validatable {

	private A action = null;
	private boolean initialized = false;

	@ApiStatus.Internal
	public final void init(A action) {

		if (action.getType() != this) {
			throw new IllegalArgumentException("Cannot initialize action type \"" + getConfig().id() + "\" with mismatched action!");
		}

		this.action = action;
		this.initialized = true;

	}

	@Override
	public abstract void accept(AX context);

	@SuppressWarnings("unchecked")
	@Override
	public void validate() throws Exception {

		TypedDataObjectFactory<ActionType<AX, A>> dataFactory = (TypedDataObjectFactory<ActionType<AX,A>>) getConfig().dataFactory();
		SerializableData.Instance data = dataFactory.toData(this);

		data.validate();

	}

	@NotNull
	public abstract ActionConfiguration<?> getConfig();

	public final A getAction() {

		if (initialized) {
			return Objects.requireNonNull(action, "Action of initialized action type \"" + getConfig().id() + "\" was null!");
		}

		else {
			throw new IllegalStateException("Action type \"" + getConfig().id() + "\" wasn't initialized yet!");
		}

	}

	public abstract A createAction();

	public boolean shouldExecute(AX context) {
		return true;
	}

}
