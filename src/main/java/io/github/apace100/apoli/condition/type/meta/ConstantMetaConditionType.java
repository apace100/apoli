package io.github.apace100.apoli.condition.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.Condition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ConditionType;
import io.github.apace100.apoli.util.context.ConditionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;

import java.util.function.Function;

public interface ConstantMetaConditionType {

    boolean value();

    static <T extends ConditionContext, C extends Condition<T, CT>, CT extends ConditionType<T, C>, M extends ConditionType<T, C> & ConstantMetaConditionType> ConditionConfiguration<M> createConfiguration(Function<Boolean, M> constructor) {
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
