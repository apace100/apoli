package io.github.apace100.apoli.power.factory.action.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.FilterableWeightedList;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.Random;

public class ChoiceAction {

    public static <T> void action(SerializableData.Instance data, T t) {
        FilterableWeightedList<ActionFactory<T>.Instance> actionList = data.get("actions");
        ActionFactory<T>.Instance action = actionList.pickRandom(new Random());
        action.accept(t);
    }

    public static <T> ActionFactory<T> getFactory(SerializableDataType<ActionFactory<T>.Instance> dataType) {
        return new ActionFactory<T>(Apoli.identifier("choice"),
            new SerializableData()
                .add("actions", SerializableDataType.weightedList(dataType)),
            ChoiceAction::action
        );
    }
}
