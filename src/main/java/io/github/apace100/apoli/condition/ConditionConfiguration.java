package io.github.apace100.apoli.condition;

import io.github.apace100.apoli.condition.type.AbstractConditionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.TypeConfiguration;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public record ConditionConfiguration<T extends AbstractConditionType<?, ?>>(Identifier id, TypedDataObjectFactory<T> dataFactory) implements TypeConfiguration<T> {

	public static <T extends AbstractConditionType<?, ?>> ConditionConfiguration<T> of(Identifier id, SerializableData serializableData, Function<SerializableData.Instance, T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
		TypedDataObjectFactory<T> dataFactory = TypedDataObjectFactory.simple(serializableData, fromData, toData);
		return of(id, dataFactory);
	}

	public static <T extends AbstractConditionType<?, ?>> ConditionConfiguration<T> of(Identifier id, TypedDataObjectFactory<T> dataFactory) {
		return new ConditionConfiguration<>(id, dataFactory);
	}
	
	public static <T extends AbstractConditionType<?, ?>> ConditionConfiguration<T> simple(Identifier id, Supplier<T> constructor) {
		return of(id, new SerializableData(), data -> constructor.get(), (t, serializableData) -> serializableData.instance());
	}

}
