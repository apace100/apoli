package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.Action;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.ActionType;
import io.github.apace100.apoli.util.context.ActionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.function.BiFunction;

public interface SideMetaActionType<T extends ActionContext<?>, A extends Action<T, ?>> {

    A action();

    Side side();

    default void executeAction(T context) {

        if (((side() == Side.CLIENT)) != Apoli.onServerSide()) {
            action().accept(context);
        }

    }

    static <T extends ActionContext<?>, A extends Action<T, AT>, AT extends ActionType<T, A>, M extends ActionType<T, A> & SideMetaActionType<T, A>> ActionConfiguration<M> createConfiguration(SerializableDataType<A> actionDataType, BiFunction<A, Side, M> constructor) {
        return ActionConfiguration.of(
            Apoli.identifier("side"),
            new SerializableData()
                .add("action", actionDataType)
                .add("side", SerializableDataType.enumValue(Side.class)),
            data -> constructor.apply(
                data.get("action"),
                data.get("side")
            ),
            (m, serializableData) -> serializableData.instance()
                .set("action", m.action())
                .set("side", m.side())
        );
    }

    enum Side {
        CLIENT, SERVER
    }

}
