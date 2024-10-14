package io.github.apace100.apoli.condition.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.condition.type.AbstractConditionType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface AnyOfMetaConditionType<T, C extends AbstractCondition<T, ? extends AbstractConditionType<T, C>>> {

    List<C> conditions();

    default <CT extends AbstractConditionType<T, C>> List<C> prepareConditions(CT base, Collection<C> conditions) {
        return conditions
            .stream()
            .peek(condition -> condition.setPowerType(base.getPowerType()))
            .toList();
    }

    static <T, C extends Predicate<T>> boolean condition(T type, Collection<C> conditions) {
        return conditions
            .stream()
            .anyMatch(condition -> condition.test(type));
    }

    static <T> ConditionTypeFactory<T> getFactory(SerializableDataType<ConditionTypeFactory<T>.Instance> conditionDataType) {
        return new ConditionTypeFactory<>(
            Apoli.identifier("any_of"),
            new SerializableData()
                .add("conditions", conditionDataType.list()),
            (data, type) -> condition(type,
                data.get("conditions")
            )
        );
    }

    static <T, C extends AbstractCondition<T, CT>, CT extends AbstractConditionType<T, C>, M extends AbstractConditionType<T, C> & AnyOfMetaConditionType<T, C>> ConditionConfiguration<M> createConfiguration(SerializableDataType<C> conditionDataType, Function<List<C>, M> constructor) {
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