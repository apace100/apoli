package io.github.apace100.apoli.power.factory.action.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.function.Function;

public class IfElseListAction {

    public static <T, U> void action(SerializableData.Instance data, T t, Function<T, U> actionToConditionTypeFunction) {

        List<Pair<ActionFactory<T>.Instance, ConditionFactory<U>.Instance>> actions = data.get("actions");
        U toEvaluate = actionToConditionTypeFunction.apply(t);

        for (Pair<ActionFactory<T>.Instance, ConditionFactory<U>.Instance> action : actions) {

            if (action.getRight().test(toEvaluate)) {
                action.getLeft().accept(t);
                break;
            }

        }

    }

    public static <T, U> ActionFactory<T> getFactory(SerializableDataType<ActionFactory<T>.Instance> actionDataType, SerializableDataType<ConditionFactory<U>.Instance> conditionDataType, Function<T, U> actionToConditionTypeFunction) {

        SerializableDataType<Pair<ActionFactory<T>.Instance, ConditionFactory<T>.Instance>> dataType = SerializableDataType.compound(
            new SerializableData()
                .add("action", actionDataType)
                .add("condition", conditionDataType),
            data -> new Pair<>(
                data.get("action"),
                data.get("condition")
            ),
            (pair, data) -> data
                .set("action", pair.getLeft())
                .set("condition", pair.getRight())
        );

        return new ActionFactory<>(
            Apoli.identifier("if_else_list"),
            new SerializableData()
                .add("actions", dataType.listOf()),
            (data, t) -> action(data, t, actionToConditionTypeFunction)
        );

    }

    public static <T> ActionFactory<T> getFactory(SerializableDataType<ActionFactory<T>.Instance> actionDataType, SerializableDataType<ConditionFactory<T>.Instance> conditionDataType) {
        return getFactory(actionDataType, conditionDataType, t -> t);
    }

}
