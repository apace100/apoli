package io.github.apace100.apoli.action.factory;

import io.github.apace100.apoli.action.type.meta.*;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.function.Consumer;
import java.util.function.Function;

public class MetaActions {

    public static <T, U> void register(SerializableDataType<ActionTypeFactory<T>.Instance> actionDataType, SerializableDataType<ConditionTypeFactory<U>.Instance> conditionDataType, Function<T, U> actionToCondition, Consumer<ActionTypeFactory<T>> registrant) {
        registrant.accept(AndActionType.getFactory(actionDataType.listOf()));
        registrant.accept(ChanceActionType.getFactory(actionDataType));
        registrant.accept(IfElseActionType.getFactory(actionDataType, conditionDataType, actionToCondition));
        registrant.accept(ChoiceActionType.getFactory(actionDataType));
        registrant.accept(IfElseListActionType.getFactory(actionDataType, conditionDataType, actionToCondition));
        registrant.accept(DelayActionType.getFactory(actionDataType));
        registrant.accept(SideActionType.getFactory(actionDataType));
        registrant.accept(NothingActionType.getFactory());
    }

}
