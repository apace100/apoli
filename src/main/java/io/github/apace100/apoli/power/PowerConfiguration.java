package io.github.apace100.apoli.power;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.TypeConfiguration;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public record PowerConfiguration<T extends PowerType>(Identifier id, TypedDataObjectFactory<T> dataFactory) implements TypeConfiguration<T> {

	public static <T extends PowerType> PowerConfiguration<T> of(Identifier id, SerializableData serializableData, Function<SerializableData.Instance, T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
		return dataFactory(id, TypedDataObjectFactory.simple(serializableData, fromData, toData));
	}

	public static <T extends PowerType> PowerConfiguration<T> conditionedOf(Identifier id, SerializableData serializableData, BiFunction<SerializableData.Instance, Optional<EntityCondition>,  T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
		return dataFactory(id, PowerType.createConditionedDataFactory(serializableData, fromData, toData));
	}

	public static <T extends PowerType> PowerConfiguration<T> dataFactory(Identifier id, TypedDataObjectFactory<T> dataFactory) {
		return new PowerConfiguration<>(id, dataFactory);
	}

	public static <T extends PowerType> PowerConfiguration<T> simple(Identifier id, Supplier<T> constructor) {
		return of(id, new SerializableData(), data -> constructor.get(), (t, serializableData) -> serializableData.instance());
	}

	public static <T extends PowerType> PowerConfiguration<T> conditionedSimple(Identifier id, Function<Optional<EntityCondition>, T> constructor) {
		return conditionedOf(id, new SerializableData(), (data, entityCondition) -> constructor.apply(entityCondition), (t, serializableData) -> serializableData.instance());
	}

}
