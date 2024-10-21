package io.github.apace100.apoli.data;

import io.github.apace100.calio.data.CompoundSerializableDataType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.registry.DataObjectFactory;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface TypedDataObjectFactory<T> extends DataObjectFactory<T> {

	CompoundSerializableDataType<T> getDataType();
	
	static <T> TypedDataObjectFactory<T> simple(SerializableData serializableData, Function<SerializableData.Instance, T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
		CompoundSerializableDataType<T> dataType = SerializableDataType.compound(serializableData, fromData, toData);
		return new TypedDataObjectFactory<>() {
			
			@Override
			public CompoundSerializableDataType<T> getDataType() {
				return dataType;
			}

			@Override
			public SerializableData getSerializableData() {
				return serializableData;
			}

			@Override
			public T fromData(SerializableData.Instance data) {
				return fromData.apply(data);
			}

			@Override
			public SerializableData.Instance toData(T t, SerializableData serializableData) {
				return toData.apply(t, serializableData);
			}

		};
	}

}
