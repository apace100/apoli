package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.Action;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.ActionType;
import io.github.apace100.apoli.util.context.ActionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.util.collection.WeightedList;

import java.util.Iterator;
import java.util.function.Function;

public interface ChoiceMetaActionType<T extends ActionContext<?>, A extends Action<T, ? extends ActionType<T, A>>> {

    WeightedList<A> actions();

    default void executeActions(T context) {

		actions().shuffle();
		Iterator<A> actionIterator = actions().iterator();

		if (actionIterator.hasNext()) {
			actionIterator.next().accept(context);
		}

    }

    static <T extends ActionContext<?>, A extends Action<T, AT>, AT extends ActionType<T, A>, M extends ActionType<T, A> & ChoiceMetaActionType<T, A>> ActionConfiguration<M> createConfiguration(SerializableDataType<A> actionDataType, Function<WeightedList<A>, M> constructor) {
        return ActionConfiguration.of(
            Apoli.identifier("choice"),
            new SerializableData()
                .add("actions", SerializableDataType.weightedList(actionDataType)),
            data -> constructor.apply(
                data.get("actions")
            ),
            (m, serializableData) -> serializableData.instance()
                .set("actions", m.actions())
        );
    }

}
