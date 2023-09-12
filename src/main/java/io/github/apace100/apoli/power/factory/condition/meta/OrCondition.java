package io.github.apace100.apoli.power.factory.condition.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.List;

public class OrCondition {

    public static <T> boolean condition(SerializableData.Instance data, T type) {
        List<ConditionFactory<T>.Instance> conditions = data.get("conditions");
        return conditions
            .stream()
            .anyMatch(condition -> condition.test(type));
    }

    public static <T> ConditionFactory<T> getFactory(SerializableDataType<ConditionFactory<T>.Instance> dataType) {
        return new ConditionFactory<>(
            Apoli.identifier("or"),
            new SerializableData()
                .add("conditions", SerializableDataType.list(dataType)),
            OrCondition::condition
        );
    }

}
