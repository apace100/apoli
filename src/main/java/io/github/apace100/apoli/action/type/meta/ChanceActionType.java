package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.math.random.Random;

import java.util.function.Consumer;

public class ChanceActionType {

    public static <T> void action(T type, Consumer<T> successAction, Consumer<T> failAction, float chance) {

        if (Random.create().nextFloat() < chance) {
            successAction.accept(type);
        }

        else {
            failAction.accept(type);
        }

    }

    public static <T> ActionTypeFactory<T> getFactory(SerializableDataType<ActionTypeFactory<T>.Instance> dataType) {
        return new ActionTypeFactory<>(
            Apoli.identifier("chance"),
            new SerializableData()
                .add("success_action", dataType)
                .add("fail_action", dataType, null)
                .add("chance", SerializableDataType.boundNumber(SerializableDataTypes.FLOAT, 0F, 1F)),
            (data, type) -> action(type,
                data.get("success_action"),
                data.getOrElse("fail_action", t -> {}),
                data.get("chance")
            )
        );
    }
}
