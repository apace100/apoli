package io.github.apace100.apoli.power.factory.action.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.function.Function;

public class IfElseAction {

    public static <T, U> void action(SerializableData.Instance data, T t, Function<T, U> actionToConditionTypeFunction) {
        ConditionFactory<U>.Instance condition = data.get("condition");
        ActionFactory<T>.Instance ifAction = data.get("if_action");
        U u = actionToConditionTypeFunction.apply(t);
        if(condition.test(u)) {
            ifAction.accept(t);
        } else if(data.isPresent("else_action")) {
            ActionFactory<T>.Instance elseAction = data.get("else_action");
            elseAction.accept(t);
        }
    }

    public static <T, U> ActionFactory<T> getFactory(
        SerializableDataType<ActionFactory<T>.Instance> actionDataType,
        SerializableDataType<ConditionFactory<U>.Instance> conditionDataType,
        Function<T, U> actionToConditionTypeFunction) {
        return new ActionFactory<T>(Apoli.identifier("if_else"),
            new SerializableData()
                .add("condition", conditionDataType)
                .add("if_action", actionDataType)
                .add("else_action", actionDataType, null),
            (inst, t) -> action(inst, t, actionToConditionTypeFunction)
        );
    }

    public static <T> ActionFactory<T> getFactory(
        SerializableDataType<ActionFactory<T>.Instance> actionDataType,
        SerializableDataType<ConditionFactory<T>.Instance> conditionDataType) {
        return getFactory(actionDataType, conditionDataType, t -> t);
    }
}
