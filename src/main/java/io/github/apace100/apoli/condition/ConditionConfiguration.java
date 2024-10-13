package io.github.apace100.apoli.condition;

import io.github.apace100.apoli.condition.type.AbstractConditionType;
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

public record ConditionConfiguration<T extends AbstractConditionType<?, ?>>(Identifier id, CompoundSerializableDataType<T> dataType, DataObjectFactory<T> dataFactory) implements TypeConfiguration<T> {

	public static <T extends AbstractConditionType<?, ?>> ConditionConfiguration<T> of(Identifier id, SerializableData serializableData, Function<SerializableData.Instance, T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
		DataObjectFactory<T> dataFactory = new SimpleDataObjectFactory<>(serializableData, fromData, toData);
		return fromDataFactory(id, dataFactory);
	}

	public static <T extends AbstractConditionType<?, ?>> ConditionConfiguration<T> fromDataFactory(Identifier id, DataObjectFactory<T> dataFactory) {
		return new ConditionConfiguration<>(id, SerializableDataType.compound(dataFactory), dataFactory);
	}

	public static <T extends AbstractConditionType<?, ?>> ConditionConfiguration<T> simple(Identifier id, Supplier<T> constructor) {
		return of(id, new SerializableData(), data -> constructor.get(), (t, serializableData) -> serializableData.instance());
	}

}
