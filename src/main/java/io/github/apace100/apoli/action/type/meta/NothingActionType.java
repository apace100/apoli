package io.github.apace100.apoli.action.type.meta;

import com.google.common.base.Suppliers;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

import java.util.function.Supplier;

public class NothingActionType {

    public static <T> ActionTypeFactory<T> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("nothing"),
            new SerializableData(),
            (data, t) -> {}
        );
    }

    public static <T>Supplier<ActionTypeFactory<T>.Instance> create(Registry<ActionTypeFactory<T>> registry) {
        return Suppliers.memoize(() -> {

            ActionTypeFactory<T> nothingFactory = registry.getOrThrow(RegistryKey.of(registry.getKey(), Apoli.identifier("nothing")));
            SerializableData serializableData = nothingFactory.getSerializableData();

            return nothingFactory.fromData(serializableData.instance());

        });
    }

}
