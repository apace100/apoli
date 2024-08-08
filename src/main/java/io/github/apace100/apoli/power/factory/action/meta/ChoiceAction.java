package io.github.apace100.apoli.power.factory.action.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.util.collection.WeightedList;

import java.util.Iterator;

public class ChoiceAction {

    public static <T> void action(SerializableData.Instance data, T t) {

        WeightedList<ActionFactory<T>.Instance> actions = data.get("actions");
        actions.shuffle();

        Iterator<ActionFactory<T>.Instance> actionIterator = actions.iterator();
        if (actionIterator.hasNext()) {
            actionIterator.next().accept(t);
        }

    }

    public static <T> ActionFactory<T> getFactory(SerializableDataType<ActionFactory<T>.Instance> dataType) {
        return new ActionFactory<T>(Apoli.identifier("choice"),
            new SerializableData()
                .add("actions", SerializableDataType.weightedList(dataType)),
            ChoiceAction::action
        );
    }
}
