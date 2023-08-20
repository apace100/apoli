package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.power.factory.condition.meta.AndCondition;
import io.github.apace100.apoli.power.factory.condition.meta.ConstantCondition;
import io.github.apace100.apoli.power.factory.condition.meta.OrCondition;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.function.Consumer;

public class MetaConditions {

    public static <T> void register(SerializableDataType<ConditionFactory<T>.Instance> dataType, Consumer<ConditionFactory<T>> registrant) {
        registrant.accept(AndCondition.getFactory(dataType));
        registrant.accept(ConstantCondition.getFactory());
        registrant.accept(OrCondition.getFactory(dataType));
    }

}
