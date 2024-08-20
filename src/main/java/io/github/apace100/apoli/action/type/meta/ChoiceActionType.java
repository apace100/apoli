package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.util.collection.WeightedList;

import java.util.Iterator;
import java.util.function.Consumer;

public class ChoiceActionType {

    public static <T> void action(T type, WeightedList<Consumer<T>> actions) {

        actions.shuffle();
        Iterator<Consumer<T>> actionIterator = actions.iterator();

        if (actionIterator.hasNext()) {
            actionIterator.next().accept(type);
        }

    }

    public static <T> ActionTypeFactory<T> getFactory(SerializableDataType<ActionTypeFactory<T>.Instance> dataType) {
        return new ActionTypeFactory<>(
            Apoli.identifier("choice"),
            new SerializableData()
                .add("actions", SerializableDataType.weightedList(dataType)),
            (data, type) -> action(type,
                data.get("actions")
            )
        );
    }
}
