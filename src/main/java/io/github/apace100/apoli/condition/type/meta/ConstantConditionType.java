package io.github.apace100.apoli.condition.type.meta;

import com.google.common.base.Suppliers;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

import java.util.function.Supplier;

public class ConstantConditionType {

    public static boolean condition(boolean value) {
        return value;
    }

    public static <T> ConditionTypeFactory<T> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("constant"),
            new SerializableData()
                .add("value", SerializableDataTypes.BOOLEAN),
            (data, type) -> condition(
                data.get("value")
            )
        );
    }

    public static <T> Supplier<ConditionTypeFactory<T>.Instance> create(Registry<ConditionTypeFactory<T>> registry, boolean value) {
        return Suppliers.memoize(() -> {

            ConditionTypeFactory<T> constantFactory = registry.getOrThrow(RegistryKey.of(registry.getKey(), Apoli.identifier("constant")));
            SerializableData serializableData = constantFactory.getSerializableData();

            return constantFactory.fromData(serializableData.instance().set("value", value));

        });
    }

}
