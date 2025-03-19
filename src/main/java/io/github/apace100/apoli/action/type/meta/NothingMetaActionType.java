package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.ActionType;
import io.github.apace100.apoli.util.context.ActionContext;

import java.util.function.Supplier;

public interface NothingMetaActionType {

    static <T extends ActionContext<?>, M extends ActionType<T, ?> & NothingMetaActionType> ActionConfiguration<M> createConfiguration(Supplier<M> constructor) {
        return ActionConfiguration.simple(Apoli.identifier("nothing"), constructor);
    }

}
