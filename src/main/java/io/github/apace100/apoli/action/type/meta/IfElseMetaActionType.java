package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.AbstractAction;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.AbstractActionType;
import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.type.AbstractConditionType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.Optional;
import java.util.function.Function;

public interface IfElseMetaActionType<AX, CX, A extends AbstractAction<AX, ?>, C extends AbstractCondition<CX, ?>> {

    C condition();

    A ifAction();

    Optional<A> elseAction();

    Function<AX, CX> contextConverter();

    default void executeAction(AX actionContext) {

        CX convertedContext = contextConverter().apply(actionContext);
        if (condition().test(convertedContext)) {
            ifAction().accept(actionContext);
        }

        else {
            elseAction().ifPresent(action -> action.accept(actionContext));
        }

    }

    static <AX, CX, A extends AbstractAction<AX, AT>, AT extends AbstractActionType<AX, A>, C extends AbstractCondition<CX, CT>, CT extends AbstractConditionType<CX, C>, M extends AbstractActionType<AX, A> & IfElseMetaActionType<AX, CX, A, C>> ActionConfiguration<M> createConfiguration(SerializableDataType<A> actionDataType, SerializableDataType<C> conditionDataType, Function<AX, CX> converter, Constructor<AX, CX, A, C, M> constructor) {
        return ActionConfiguration.of(
            Apoli.identifier("if_else"),
            new SerializableData()
                .add("condition", conditionDataType)
                .add("if_action", actionDataType)
                .add("else_action", actionDataType.optional(), Optional.empty()),
            data -> constructor.create(
                data.get("condition"),
                data.get("if_action"),
                data.get("else_action"),
                converter
            ),
            (m, serializableData) -> serializableData.instance()
                .set("condition", m.condition())
                .set("if_action", m.ifAction())
                .set("else_action", m.elseAction())
        );
    }

    interface Constructor<AX, CX, A extends AbstractAction<AX, ?>, C extends AbstractCondition<CX, ?>, M extends AbstractActionType<AX, ?> & IfElseMetaActionType<AX, CX, A, C>> {
        M create(C condition, A ifAction, Optional<A> elseAction, Function<AX, CX> contextConverter);
    }

}
