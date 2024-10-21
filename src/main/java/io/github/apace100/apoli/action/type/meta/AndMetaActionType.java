package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.AbstractAction;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.AbstractActionType;
import io.github.apace100.apoli.util.context.TypeActionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.List;
import java.util.function.Function;

public interface AndMetaActionType<T extends TypeActionContext<?>, A extends AbstractAction<T, ? extends AbstractActionType<T, A>>> {

    List<A> actions();

    default void executeActions(T context) {
        actions().forEach(action -> action.accept(context));
    }

    static <T extends TypeActionContext<?>, A extends AbstractAction<T, AT>, AT extends AbstractActionType<T, A>, M extends AbstractActionType<T, A> & AndMetaActionType<T, A>> ActionConfiguration<M> createConfiguration(SerializableDataType<A> actionDataType, Function<List<A>, M> constructor) {
        return ActionConfiguration.of(
            Apoli.identifier("and"),
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
