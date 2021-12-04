package io.github.apace100.apoli.power.factory.action.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.Scheduler;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;

public class DelayAction {

    private static final Scheduler SCHEDULER = new Scheduler();

    public static <T> void action(SerializableData.Instance data, T t) {
        ActionFactory<T>.Instance action = data.get("action");
        SCHEDULER.queue(s -> action.accept(t), data.getInt("ticks"));
    }

    public static <T> ActionFactory<T> getFactory(SerializableDataType<ActionFactory<T>.Instance> dataType) {
        return new ActionFactory<T>(Apoli.identifier("delay"),
            new SerializableData()
                .add("ticks", SerializableDataTypes.INT)
                .add("action", dataType),
            DelayAction::action
        );
    }
}
