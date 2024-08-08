package io.github.apace100.apoli.power.factory.action;

import io.github.apace100.apoli.power.factory.Factory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ActionFactory<T> implements Factory {

    protected final Identifier id;

    protected final Function<SerializableData.Instance, Consumer<T>> effectFactory;
    protected final SerializableData serializableData;

    public ActionFactory(Identifier id, SerializableData serializableData, @NotNull BiConsumer<SerializableData.Instance, T> effect) {
        this.id = id;
        this.effectFactory = data -> t -> effect.accept(data, t);
        this.serializableData = serializableData.copy();
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
        return new Instance(this.getSerializableData().receive(buffer));
    }

    @Override
    public Instance fromData(SerializableData.Instance data) {
        return new Instance(data);
    }

    public class Instance implements Factory.Instance, Consumer<T> {

        protected final SerializableData.Instance data;
        protected final Consumer<T> effect;

        protected Instance(SerializableData.Instance data) {
            this.effect = effectFactory.apply(data);
            this.data = data;
        }

        @Override
        public void accept(T t) {
            effect.accept(t);
        }

        @Override
        public SerializableData.Instance getData() {
            return data;
        }

        @Override
        public ActionFactory<T> getFactory() {
            return ActionFactory.this;
        }

        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }

            else if (obj instanceof ActionFactory<?>.Instance other) {
                return this.getData().equals(other.getData())
                    && this.getFactory().equals(other.getFactory());
            }

            else {
                return false;
            }

        }

    }

}
