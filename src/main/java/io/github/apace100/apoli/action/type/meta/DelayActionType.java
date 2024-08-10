package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.util.Scheduler;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;

import java.util.function.Consumer;

public class DelayActionType {

    private static final Scheduler SCHEDULER = new Scheduler();

    public static <T> void action(T type, Consumer<T> action, int ticks) {
        SCHEDULER.queue(server -> action.accept(type), ticks);
    }

    public static <T> ActionTypeFactory<T> getFactory(SerializableDataType<ActionTypeFactory<T>.Instance> dataType) {
        return new ActionTypeFactory<>(
            Apoli.identifier("delay"),
            new SerializableData()
                .add("action", dataType)
                .add("ticks", SerializableDataTypes.INT),
            (data, type) -> action(type,
                data.get("action"),
                data.get("ticks")
            )
        );
    }

}
