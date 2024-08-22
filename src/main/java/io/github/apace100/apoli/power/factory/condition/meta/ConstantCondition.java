package io.github.apace100.apoli.power.factory.condition.meta;

import com.google.common.base.Suppliers;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

import java.util.function.Supplier;

public class ConstantCondition {

    public static <T> boolean condition(SerializableData.Instance data, T type) {
        return data.get("value");
    }

    public static <T> ConditionFactory<T> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("constant"),
            new SerializableData()
                .add("value", SerializableDataTypes.BOOLEAN),
            ConstantCondition::condition
        );
    }

    public static <T> Supplier<ConditionFactory<T>.Instance> create(Registry<ConditionFactory<T>> registry, boolean value) {
        return Suppliers.memoize(() -> {

            ConditionFactory<T> constantFactory = registry.getOrThrow(RegistryKey.of(registry.getKey(), Apoli.identifier("constant")));
            SerializableData serializableData = constantFactory.getSerializableData();

            return constantFactory.fromData(serializableData.instance().set("value", value));

        });
    }

}
