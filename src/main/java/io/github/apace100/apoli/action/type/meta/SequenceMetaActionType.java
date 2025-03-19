package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.Action;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.ActionType;
import io.github.apace100.apoli.util.context.ActionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.List;
import java.util.function.Function;

public interface SequenceMetaActionType<T extends ActionContext<?>, A extends Action<T, ? extends ActionType<T, A>>> {

    List<A> actions();

    default void executeActions(T context) {
        actions().forEach(action -> action.accept(context));
    }

    static <T extends ActionContext<?>, A extends Action<T, AT>, AT extends ActionType<T, A>, M extends ActionType<T, A> & SequenceMetaActionType<T, A>> ActionConfiguration<M> createConfiguration(SerializableDataType<A> actionDataType, Function<List<A>, M> constructor) {
        return ActionConfiguration.of(
            Apoli.identifier("sequence"),
            new SerializableData()
                .add("actions", actionDataType.list()),
            data -> constructor.apply(
                data.get("actions")
            ),
            (m, serializableData) -> serializableData.instance()
                .set("actions", m.actions())
        );
    }

}
