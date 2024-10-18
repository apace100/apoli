package io.github.apace100.apoli.action;

import io.github.apace100.apoli.action.type.AbstractActionType;
import io.github.apace100.apoli.util.TypeConfiguration;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public record ActionConfiguration<T extends AbstractActionType<?, ?>>(Identifier id, CompoundSerializableDataType<T> dataType, DataObjectFactory<T> dataFactory) implements TypeConfiguration<T> {

	public static <T extends AbstractActionType<?, ?>> ActionConfiguration<T> of(Identifier id, SerializableData serializableData, Function<SerializableData.Instance, T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
		DataObjectFactory<T> dataFactory = new SimpleDataObjectFactory<>(serializableData, fromData, toData);
		return fromDataFactory(id, dataFactory);
	}

	public static <T extends AbstractActionType<?, ?>> ActionConfiguration<T> simple(Identifier id, Supplier<T> constructor) {
		return of(id, new SerializableData(), data -> constructor.get(), (t, serializableData) -> serializableData.instance());
	}

	public static <T extends AbstractActionType<?, ?>> ActionConfiguration<T> fromDataFactory(Identifier id, DataObjectFactory<T> dataFactory) {
		return new ActionConfiguration<>(id, SerializableDataType.compound(dataFactory), dataFactory);
	}

}
