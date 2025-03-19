package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.Action;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.ActionType;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.context.ActionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Optional;

public interface RandomChanceMetaActionType<T extends ActionContext<?>, A extends Action<T, ? extends ActionType<T, A>>> {

    A successAction();

    Optional<A> failAction();

    float chance();

    default void executeAction(T context) {

        if (Random.create().nextFloat() < chance()) {
            successAction().accept(context);
        }

        else {
            failAction().ifPresent(action -> action.accept(context));
        }

    }

    static <T extends ActionContext<?>, A extends Action<T, AT>, AT extends ActionType<T, A>, M extends ActionType<T, A> & RandomChanceMetaActionType<T, A>> ActionConfiguration<M> createConfiguration(SerializableDataType<A> actionDataType, TriFunction<A, Optional<A>, Float, M> constructor) {
        return ActionConfiguration.of(
            Apoli.identifier("random_chance"),
            new SerializableData()
                .add("action", actionDataType, null)
                .addFunctionedDefault("success_action", actionDataType, data -> data.get("action"))
                .add("fail_action", actionDataType.optional(), Optional.empty())
                .add("chance", ApoliDataTypes.NORMALIZED_FLOAT)
                .validate(MiscUtil.validateAnyFieldsPresent("action", "success_action")),
            data -> constructor.apply(
                data.get("success_action"),
                data.get("fail_action"),
                data.get("chance")
            ),
            (m, serializableData) -> serializableData.instance()
                .set("success_action", m.successAction())
                .set("fail_action", m.failAction())
                .set("chance", m.chance())
        );
    }

}
