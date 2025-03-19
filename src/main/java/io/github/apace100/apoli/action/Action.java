package io.github.apace100.apoli.action;

import io.github.apace100.apoli.action.type.ActionType;
import io.github.apace100.apoli.util.context.ActionContext;
import io.github.apace100.calio.util.Validatable;

import java.util.function.Consumer;

public abstract class Action<AX extends ActionContext<?>, AT extends ActionType<AX, ?>> implements Consumer<AX>, Validatable {

	private final AT actionType;

	public Action(AT actionType) {
		this.actionType = actionType;
		//noinspection unchecked
		((ActionType<AX, Action<AX, AT>>) this.actionType).init(this);
	}

	@Override
	public void accept(AX context) {

		if (getType().shouldExecute(context)) {
			getType().accept(context);
		}

	}

	@Override
	public void validate() throws Exception {
		getType().validate();
	}

	public final AT getType() {
		return actionType;
	}

}
