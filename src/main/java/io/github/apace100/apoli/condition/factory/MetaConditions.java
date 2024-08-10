package io.github.apace100.apoli.condition.factory;

import io.github.apace100.apoli.condition.type.meta.AndConditionType;
import io.github.apace100.apoli.condition.type.meta.ChanceConditionType;
import io.github.apace100.apoli.condition.type.meta.ConstantConditionType;
import io.github.apace100.apoli.condition.type.meta.OrConditionType;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.function.Consumer;

public class MetaConditions {

    public static <T> void register(SerializableDataType<ConditionTypeFactory<T>.Instance> dataType, Consumer<ConditionTypeFactory<T>> registrant) {
        registrant.accept(AndConditionType.getFactory(dataType));
        registrant.accept(ConstantConditionType.getFactory());
        registrant.accept(OrConditionType.getFactory(dataType));
        registrant.accept(ChanceConditionType.getFactory());
    }

}
