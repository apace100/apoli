package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.function.Function;

public class SideActionType {

    public static <T> void action(T type, ActionTypeFactory<T>.Instance action, Side side, Function<T, Boolean> serverCheck) {

        if ((side == Side.CLIENT) != serverCheck.apply(type)) {
            action.accept(type);
        }

    }

    public static <T> ActionTypeFactory<T> getFactory(SerializableDataType<ActionTypeFactory<T>.Instance> dataType, Function<T, Boolean> serverCheck) {
        return new ActionTypeFactory<>(
            Apoli.identifier("side"),
            new SerializableData()
                .add("action", dataType)
                .add("side", SerializableDataType.enumValue(Side.class)),
            (data, type) -> SideActionType.action(type,
                data.get("action"),
                data.get("side"),
                serverCheck
            )
        );
    }

    public static <T> ActionTypeFactory<T> getFactory(SerializableDataType<ActionTypeFactory<T>.Instance> dataType) {
        return getFactory(dataType, t -> Apoli.server != null && Apoli.server.isOnThread());
    }

    public enum Side {
        CLIENT, SERVER
    }

}
