package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.condition.type.meta.AllOfConditionType;
import io.github.apace100.apoli.condition.type.meta.RandomChanceConditionType;
import io.github.apace100.apoli.condition.type.meta.ConstantConditionType;
import io.github.apace100.apoli.condition.type.meta.AnyOfConditionType;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.function.Consumer;

public class MetaConditionTypes {

    public static <T> void register(SerializableDataType<ConditionTypeFactory<T>.Instance> dataType, Consumer<ConditionTypeFactory<T>> registrant) {
        registrant.accept(AllOfConditionType.getFactory(dataType));
        registrant.accept(ConstantConditionType.getFactory());
        registrant.accept(AnyOfConditionType.getFactory(dataType));
        registrant.accept(RandomChanceConditionType.getFactory());
    }

}
