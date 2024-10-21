package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.AbstractAction;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.AbstractActionType;
import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.type.AbstractConditionType;
import io.github.apace100.apoli.util.context.TypeActionContext;
import io.github.apace100.apoli.util.context.TypeConditionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.Optional;

public interface IfElseMetaActionType<AX extends TypeActionContext<CX>, CX extends TypeConditionContext, A extends AbstractAction<AX, ?>, C extends AbstractCondition<CX, ?>> {

    C condition();

    A ifAction();

    Optional<A> elseAction();

    default void executeAction(AX actionContext) {

        if (condition().test(actionContext.conditionContext())) {
            ifAction().accept(actionContext);
        }

        else {
            elseAction().ifPresent(action -> action.accept(actionContext));
        }

    }

    static <AX extends TypeActionContext<CX>, CX extends TypeConditionContext, A extends AbstractAction<AX, AT>, AT extends AbstractActionType<AX, A>, C extends AbstractCondition<CX, CT>, CT extends AbstractConditionType<CX, C>, M extends AbstractActionType<AX, A> & IfElseMetaActionType<AX, CX, A, C>> ActionConfiguration<M> createConfiguration(SerializableDataType<A> actionDataType, SerializableDataType<C> conditionDataType, Constructor<AX, CX, A, C, M> constructor) {
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

    interface Constructor<AX extends TypeActionContext<CX>, CX extends TypeConditionContext, A extends AbstractAction<AX, ?>, C extends AbstractCondition<CX, ?>, M extends AbstractActionType<AX, ?> & IfElseMetaActionType<AX, CX, A, C>> {
        M create(C condition, A ifAction, Optional<A> elseAction);
    }

}
