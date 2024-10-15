package io.github.apace100.apoli.action.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.AbstractActionType;

import java.util.function.Supplier;

public interface NothingMetaActionType {

    static <T, M extends AbstractActionType<T, ?> & NothingMetaActionType> ActionConfiguration<M> createConfiguration(Supplier<M> constructor) {
        return ActionConfiguration.simple(Apoli.identifier("nothing"), constructor);
    }

}
