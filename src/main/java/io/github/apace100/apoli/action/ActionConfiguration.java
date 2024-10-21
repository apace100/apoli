package io.github.apace100.apoli.action;

import io.github.apace100.apoli.action.type.AbstractActionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.TypeConfiguration;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public record ActionConfiguration<T extends AbstractActionType<?, ?>>(Identifier id, TypedDataObjectFactory<T> dataFactory) implements TypeConfiguration<T> {

	public static <T extends AbstractActionType<?, ?>> ActionConfiguration<T> of(Identifier id, SerializableData serializableData, Function<SerializableData.Instance, T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
		TypedDataObjectFactory<T> dataFactory = TypedDataObjectFactory.simple(serializableData, fromData, toData);
		return of(id, dataFactory);
	}

	public static <T extends AbstractActionType<?, ?>> ActionConfiguration<T> of(Identifier id, TypedDataObjectFactory<T> dataFactory) {
		return new ActionConfiguration<>(id, dataFactory);
	}

	public static <T extends AbstractActionType<?, ?>> ActionConfiguration<T> simple(Identifier id, Supplier<T> constructor) {
		return of(id, new SerializableData(), data -> constructor.get(), (t, serializableData) -> serializableData.instance());
	}

}
