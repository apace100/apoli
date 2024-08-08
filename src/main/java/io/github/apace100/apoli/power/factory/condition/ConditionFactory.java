package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.power.factory.Factory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class ConditionFactory<T> implements Factory {

    protected final Identifier id;
    protected final Function<SerializableData.Instance, Predicate<T>> conditionFactory;

    protected final SerializableData serializableData;

    public ConditionFactory(Identifier id, SerializableData serializableData, BiFunction<SerializableData.Instance, T, Boolean> condition) {
        this.id = id;
        this.conditionFactory = data -> t -> condition.apply(data, t);
        this.serializableData = serializableData.copy().add("inverted", SerializableDataTypes.BOOLEAN, false);
    }

    @Override
    public Identifier getSerializerId() {
        return id;
    }

    @Override
    public SerializableData getSerializableData() {
        return serializableData;
    }

    @Override
    public Instance receive(RegistryByteBuf buffer) {
        return new Instance(serializableData.receive(buffer));
    }

    @Override
    public Instance fromData(SerializableData.Instance data) {
        return new Instance(data);
    }

    public class Instance implements Factory.Instance, Predicate<T> {

        protected final SerializableData.Instance data;
        protected final Predicate<T> condition;

        protected Instance(SerializableData.Instance data) {
            this.condition = conditionFactory.apply(data);
            this.data = data;
        }

        @Override
        public SerializableData.Instance getData() {
            return data;
        }

        @Override
        public ConditionFactory<T> getFactory() {
            return ConditionFactory.this;
        }

        @Override
        public boolean test(T t) {
            return data.getBoolean("inverted") != condition.test(t);
        }

        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }

            else if (obj instanceof ConditionFactory<?>.Instance other) {
                return this.getData().equals(other.getData())
                    && this.getFactory().equals(other.getFactory());
            }

            else {
                return false;
            }

        }

    }

}
