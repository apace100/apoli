package io.github.apace100.apoli.power.factory.action.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.function.Function;

public class SideAction {

    public static <T> void action(SerializableData.Instance data, T t, Function<T, Boolean> serverCheck) {
        ActionFactory<T>.Instance action = data.get("action");
        Side side = data.get("side");
        boolean isServer = serverCheck.apply(t);
        if((side == Side.CLIENT) != isServer) {
            action.accept(t);
        }
    }

    public static <T> ActionFactory<T> getFactory(SerializableDataType<ActionFactory<T>.Instance> dataType, Function<T, Boolean> serverCheck) {
        return new ActionFactory<T>(Apoli.identifier("side"),
            new SerializableData()
                .add("side", SerializableDataType.enumValue(Side.class))
                .add("action", dataType),
            (data, t) -> SideAction.action(data, t, serverCheck)
        );
    }

    public enum Side {
        CLIENT, SERVER
    }
}
