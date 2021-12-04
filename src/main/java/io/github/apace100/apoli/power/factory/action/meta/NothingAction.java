package io.github.apace100.apoli.power.factory.action.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;

public class NothingAction {

    public static <T> ActionFactory<T> getFactory() {
        return new ActionFactory<T>(Apoli.identifier("nothing"),
            new SerializableData(),
            (inst, t) -> {}
        );
    }
}
