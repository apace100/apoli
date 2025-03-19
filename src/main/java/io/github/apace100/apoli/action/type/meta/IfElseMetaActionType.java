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

import java.util.Optional;

public interface IfElseMetaActionType<AX extends ActionContext<CX>, CX extends ConditionContext, A extends Action<AX, ?>, C extends Condition<CX, ?>> {

    C condition();

    A ifAction();

    Optional<A> elseAction();

    default void executeAction(AX actionContext) {

        if (condition().test(actionContext.forCondition())) {
            ifAction().accept(actionContext);
        }

        else {
            elseAction().ifPresent(action -> action.accept(actionContext));
        }

    }

    static <AX extends ActionContext<CX>, CX extends ConditionContext, A extends Action<AX, AT>, AT extends ActionType<AX, A>, C extends Condition<CX, CT>, CT extends ConditionType<CX, C>, M extends ActionType<AX, A> & IfElseMetaActionType<AX, CX, A, C>> ActionConfiguration<M> createConfiguration(SerializableDataType<A> actionDataType, SerializableDataType<C> conditionDataType, Constructor<AX, CX, A, C, M> constructor) {
        return ActionConfiguration.of(
            Apoli.identifier("if_else"),
            new SerializableData()
                .add("condition", conditionDataType)
                .add("if_action", actionDataType)
                .add("else_action", actionDataType.optional(), Optional.empty()),
            data -> constructor.create(
                data.get("condition"),
                data.get("if_action"),
                data.get("else_action")
            ),
            (m, serializableData) -> serializableData.instance()
                .set("condition", m.condition())
                .set("if_action", m.ifAction())
                .set("else_action", m.elseAction())
        );
    }

    interface Constructor<AX extends ActionContext<CX>, CX extends ConditionContext, A extends Action<AX, ?>, C extends Condition<CX, ?>, M extends ActionType<AX, ?> & IfElseMetaActionType<AX, CX, A, C>> {
        M create(C condition, A ifAction, Optional<A> elseAction);
    }

}
