package io.github.apace100.apoli.power.factory.action.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.List;

public class AndAction {

    public static <T> void action(SerializableData.Instance data, T t) {
        List<ActionFactory<T>.Instance> actions = data.get("actions");
        actions.forEach(a -> a.accept(t));
    }

    public static <T> ActionFactory<T> getFactory(SerializableDataType<List<ActionFactory<T>.Instance>> listDataType) {
        return new ActionFactory<T>(Apoli.identifier("and"),
            new SerializableData()
                .add("actions", listDataType),
            AndAction::action
        );
    }
}
