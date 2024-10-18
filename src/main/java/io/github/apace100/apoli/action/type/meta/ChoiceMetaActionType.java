package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.AbstractAction;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.AbstractActionType;
import io.github.apace100.apoli.util.context.TypeActionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.util.collection.WeightedList;

import java.util.function.Function;

public interface ChoiceMetaActionType<T extends TypeActionContext<?>, A extends AbstractAction<T, ? extends AbstractActionType<T, A>>> {

    WeightedList<A> actions();

    default void executeActions(T context) {

        actions().shuffle();

		for (A a : actions()) {
			a.accept(context);
		}

    }

    static <T extends TypeActionContext<?>, A extends AbstractAction<T, AT>, AT extends AbstractActionType<T, A>, M extends AbstractActionType<T, A> & ChoiceMetaActionType<T, A>> ActionConfiguration<M> createConfiguration(SerializableDataType<A> actionDataType, Function<WeightedList<A>, M> constructor) {
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
