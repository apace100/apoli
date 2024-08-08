package io.github.apace100.apoli.power.factory.action.meta;

import com.google.common.base.Suppliers;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

import java.util.function.Supplier;

public class NothingAction {

    public static <T> ActionFactory<T> getFactory() {
        return new ActionFactory<T>(Apoli.identifier("nothing"),
            new SerializableData(),
            (inst, t) -> {}
        );
    }

    public static <T>Supplier<ActionFactory<T>.Instance> create(Registry<ActionFactory<T>> registry) {
        return Suppliers.memoize(() -> {

            ActionFactory<T> nothingFactory = registry.getOrThrow(RegistryKey.of(registry.getKey(), Apoli.identifier("nothing")));
            SerializableData serializableData = nothingFactory.getSerializableData();

            return nothingFactory.fromData(serializableData.instance());

        });
    }

}
