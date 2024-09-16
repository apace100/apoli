package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.util.Pair;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class IfElseListActionType {

    public static <T, U> void action(T type, Collection<Pair<Consumer<T>, Predicate<U>>> actions, Function<T, U> actionToConditionTypeFunction) {

        U convertedType = actionToConditionTypeFunction.apply(type);

        for (Pair<Consumer<T>, Predicate<U>> action : actions) {

            if (action.getRight().test(convertedType)) {
                action.getLeft().accept(type);
                break;
            }

        }

    }

    public static <T, U> ActionTypeFactory<T> getFactory(SerializableDataType<ActionTypeFactory<T>.Instance> actionDataType, SerializableDataType<ConditionTypeFactory<U>.Instance> conditionDataType, Function<T, U> actionToConditionTypeFunction) {

        SerializableDataType<Pair<ActionTypeFactory<T>.Instance, ConditionTypeFactory<T>.Instance>> dataType = SerializableDataType.compound(
            new SerializableData()
                .add("action", actionDataType)
                .add("condition", conditionDataType),
            data -> new Pair<>(
                data.get("action"),
                data.get("condition")
            ),
            (pair, serializableData) -> serializableData.instance()
                .set("action", pair.getLeft())
                .set("condition", pair.getRight())
        );

        return new ActionTypeFactory<>(
            Apoli.identifier("if_else_list"),
            new SerializableData()
                .add("actions", dataType.list()),
            (data, t) -> action(t,
                data.get("actions"),
                actionToConditionTypeFunction
            )
        );

    }

    public static <T> ActionTypeFactory<T> getFactory(SerializableDataType<ActionTypeFactory<T>.Instance> actionDataType, SerializableDataType<ConditionTypeFactory<T>.Instance> conditionDataType) {
        return getFactory(actionDataType, conditionDataType, t -> t);
    }

}
