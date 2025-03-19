package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.Action;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.ActionType;
import io.github.apace100.apoli.condition.Condition;
import io.github.apace100.apoli.condition.type.ConditionType;
import io.github.apace100.apoli.util.context.ActionContext;
import io.github.apace100.apoli.util.context.ConditionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.List;
import java.util.function.Function;

public interface IfElseListMetaActionType<AX extends ActionContext<CX>, CX extends ConditionContext, A extends Action<AX, ?>, C extends Condition<CX, ?>> {

    List<ConditionedAction<A, C>> conditionedActions();

    default void executeActions(AX actionContext) {

        CX convertedContext = actionContext.forCondition();

        for (ConditionedAction<A, C> conditionedAction : conditionedActions()) {

            if (conditionedAction.condition().test(convertedContext)) {
                conditionedAction.action().accept(actionContext);
                break;
            }

        }

    }

    static <AX extends ActionContext<CX>, CX extends ConditionContext, A extends Action<AX, AT>, AT extends ActionType<AX, A>, C extends Condition<CX, CT>, CT extends ConditionType<CX, C>, M extends ActionType<AX, A> & IfElseListMetaActionType<AX, CX, A, C>> ActionConfiguration<M> createConfiguration(SerializableDataType<A> actionDataType, SerializableDataType<C> conditionDataType, Function<List<ConditionedAction<A, C>>, M> constructor) {

        SerializableDataType<ConditionedAction<A, C>> conditionedActionDataType = SerializableDataType.compound(
            new SerializableData()
                .add("action", actionDataType)
                .add("condition", conditionDataType),
            data -> new ConditionedAction<>(
                data.get("action"),
                data.get("condition")
            ),
            (conditionedAction, serializableData) -> serializableData.instance()
                .set("action", conditionedAction.action())
                .set("condition", conditionedAction.condition())
        );

        return ActionConfiguration.of(
            Apoli.identifier("if_else_list"),
            new SerializableData()
                .add("actions", conditionedActionDataType.list()),
            data -> constructor.apply(
                data.get("actions")
            ),
            (m, serializableData) -> serializableData.instance()
                .set("actions", m.conditionedActions())
        );

    }

    record ConditionedAction<A extends Action<?, ?>, C extends Condition<?, ?>>(A action, C condition) {

    }

}
