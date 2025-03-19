package io.github.apace100.apoli.condition.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.Condition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ConditionType;
import io.github.apace100.apoli.util.context.ConditionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.List;
import java.util.function.Function;

public interface AllOfMetaConditionType<CX extends ConditionContext, CC extends Condition<CX, ? extends ConditionType<CX, CC>>> extends MultiMetaConditionType<CX, CC> {

    default boolean testConditions(CX context) {
        return conditions()
            .stream()
            .allMatch(condition -> condition.test(context));
    }

    static <T extends ConditionContext, C extends Condition<T, CT>, CT extends ConditionType<T, C>, M extends ConditionType<T, C> & AllOfMetaConditionType<T, C>> ConditionConfiguration<M> createConfiguration(SerializableDataType<C> conditionDataType, Function<List<C>, M> constructor) {
        return ConditionConfiguration.of(
            Apoli.identifier("all_of"),
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
