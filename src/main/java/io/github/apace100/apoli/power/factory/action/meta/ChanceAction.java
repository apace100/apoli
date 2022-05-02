package io.github.apace100.apoli.power.factory.action.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;

import java.util.Random;

public class ChanceAction {

    public static <T> void action(SerializableData.Instance data, T t) {
        ActionFactory<T>.Instance action = data.get("action");
        if(new Random().nextFloat() < data.getFloat("chance")) {
            action.accept(t);
        } else if(data.isPresent("fail_action")) {
            ActionFactory<T>.Instance fail = data.get("fail_action");
            fail.accept(t);
        }
    }

    public static <T> ActionFactory<T> getFactory(SerializableDataType<ActionFactory<T>.Instance> dataType) {
        return new ActionFactory<T>(Apoli.identifier("chance"),
            new SerializableData()
                .add("action", dataType)
                .add("chance", SerializableDataTypes.FLOAT)
                .add("fail_action", dataType, null),
            ChanceAction::action
        );
    }
}
