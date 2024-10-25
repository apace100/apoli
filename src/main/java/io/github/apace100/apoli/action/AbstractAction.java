package io.github.apace100.apoli.action;

import io.github.apace100.apoli.action.type.AbstractActionType;
import io.github.apace100.apoli.util.context.TypeActionContext;
import io.github.apace100.calio.util.Validatable;

import java.util.function.Consumer;

public abstract class AbstractAction<T extends TypeActionContext<?>, AT extends AbstractActionType<T, ?>> implements Consumer<T>, Validatable {

	private final AT actionType;

	public AbstractAction(AT actionType) {
		this.actionType = actionType;
		//noinspection unchecked
		((AbstractActionType<T, AbstractAction<T, AT>>) this.actionType).init(this);
	}

	@Override
	public void accept(T context) {
		getActionType().accept(context);
	}

	@Override
	public void validate() throws Exception {
		getActionType().validate();
	}

	public final AT getActionType() {
		return actionType;
	}

}
