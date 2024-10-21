package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.AbstractAction;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.AbstractActionType;
import io.github.apace100.apoli.util.Scheduler;
import io.github.apace100.apoli.util.context.TypeActionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;

import java.util.function.BiFunction;

public interface DelayMetaActionType<T extends TypeActionContext<?>, A extends AbstractAction<T, ? extends AbstractActionType<T, A>>> {

    Scheduler SCHEDULER = new Scheduler();

    A action();

    int ticks();

    default void executeAction(T context) {
        SCHEDULER.queue(server -> action().accept(context), ticks());
    }

    static <T extends TypeActionContext<?>, A extends AbstractAction<T, AT>, AT extends AbstractActionType<T, A>, M extends AbstractActionType<T, A> & DelayMetaActionType<T, A>> ActionConfiguration<M> createConfiguration(SerializableDataType<A> actionDataType, BiFunction<A, Integer, M> constructor) {
        return ActionConfiguration.of(
            Apoli.identifier("delay"),
            new SerializableData()
                .add("action", actionDataType)
                .add("ticks", SerializableDataTypes.POSITIVE_INT),
            data -> constructor.apply(
                data.get("action"),
                data.get("ticks")
            ),
            (m, serializableData) -> serializableData.instance()
                .set("action", m.action())
                .set("ticks", m.ticks())
        );
    }

}
