package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class AndActionType {

    public static <T> void action(T type, Collection<Consumer<T>> actions) {
        actions.forEach(action -> action.accept(type));
    }

    public static <T> ActionTypeFactory<T> getFactory(SerializableDataType<List<ActionTypeFactory<T>.Instance>> listDataType) {
        return new ActionTypeFactory<>(
            Apoli.identifier("and"),
            new SerializableData()
                .add("actions", listDataType),
            (data, type) -> action(type,
                data.get("actions")
            )
        );
    }
}
