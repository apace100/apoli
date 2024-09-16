package io.github.apace100.apoli.condition.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.Collection;
import java.util.function.Predicate;

public class OrConditionType {

    public static <T> boolean condition(T type, Collection<Predicate<T>> conditions) {
        return conditions
            .stream()
            .anyMatch(condition -> condition.test(type));
    }

    public static <T> ConditionTypeFactory<T> getFactory(SerializableDataType<ConditionTypeFactory<T>.Instance> conditionDataType) {
        return new ConditionTypeFactory<>(
            Apoli.identifier("or"),
            new SerializableData()
                .add("conditions", conditionDataType.list()),
            (data, type) -> condition(type,
                data.get("conditions")
            )
        );
    }

}
