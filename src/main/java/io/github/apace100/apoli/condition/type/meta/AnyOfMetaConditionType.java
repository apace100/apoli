package io.github.apace100.apoli.condition.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.AbstractConditionType;
import io.github.apace100.apoli.util.context.TypeConditionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.List;
import java.util.function.Function;

public interface AnyOfMetaConditionType<T extends TypeConditionContext, C extends AbstractCondition<T, ? extends AbstractConditionType<T, C>>> {

    List<C> conditions();

    default boolean testConditions(T context) {
        return conditions()
            .stream()
            .anyMatch(condition -> condition.test(context));
    }

    static <T extends TypeConditionContext, C extends AbstractCondition<T, CT>, CT extends AbstractConditionType<T, C>, M extends AbstractConditionType<T, C> & AnyOfMetaConditionType<T, C>> ConditionConfiguration<M> createConfiguration(SerializableDataType<C> conditionDataType, Function<List<C>, M> constructor) {
        return ConditionConfiguration.of(
            Apoli.identifier("any_of"),
            new SerializableData()
                .add("conditions", conditionDataType.list()),
            data -> constructor.apply(
                data.get("conditions")
            ),
            (m, serializableData) -> serializableData.instance()
                .set("conditions", m.conditions())
        );
    }

}
