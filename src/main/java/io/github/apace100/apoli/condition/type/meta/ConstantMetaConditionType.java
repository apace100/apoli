package io.github.apace100.apoli.condition.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.condition.type.AbstractConditionType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;

import java.util.function.Function;

public interface ConstantMetaConditionType {

    boolean value();

    static boolean condition(boolean value) {
        return value;
    }

    static <T> ConditionTypeFactory<T> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("constant"),
            new SerializableData()
                .add("value", SerializableDataTypes.BOOLEAN),
            (data, type) -> condition(
                data.get("value")
            )
        );
    }

    static <T, C extends AbstractCondition<T, CT>, CT extends AbstractConditionType<T, C>, M extends AbstractConditionType<T, C> & ConstantMetaConditionType> ConditionConfiguration<M> createConfiguration(Function<Boolean, M> constructor) {
        return ConditionConfiguration.of(
            Apoli.identifier("constant"),
            new SerializableData()
                .add("value", SerializableDataTypes.BOOLEAN),
            data -> constructor.apply(
                data.get("value")
            ),
            (m, serializableData) -> serializableData.instance()
                .set("value", m.value())
        );
    }

}
