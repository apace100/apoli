package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.condition.type.meta.AllOfMetaConditionType;
import io.github.apace100.apoli.condition.type.meta.AnyOfMetaConditionType;
import io.github.apace100.apoli.condition.type.meta.ConstantMetaConditionType;
import io.github.apace100.apoli.condition.type.meta.RandomChanceMetaConditionType;
import io.github.apace100.calio.data.SerializableDataType;

import java.util.function.Consumer;

public class MetaConditionTypes {

    public static <T> void register(SerializableDataType<ConditionTypeFactory<T>.Instance> dataType, Consumer<ConditionTypeFactory<T>> registrant) {
        registrant.accept(AllOfMetaConditionType.getFactory(dataType));
        registrant.accept(ConstantMetaConditionType.getFactory());
        registrant.accept(AnyOfMetaConditionType.getFactory(dataType));
        registrant.accept(RandomChanceMetaConditionType.getFactory());
    }

}
