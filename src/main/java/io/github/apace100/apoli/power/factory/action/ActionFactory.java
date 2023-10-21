package io.github.apace100.apoli.power.factory.action;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.factory.Factory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ActionFactory<T> implements Factory {

    private final Identifier identifier;
    private final BiConsumer<SerializableData.Instance, T> effect;

    protected final SerializableData data;

    public ActionFactory(Identifier identifier, SerializableData data, BiConsumer<SerializableData.Instance, T> effect) {
        this.identifier = identifier;
        this.effect = effect;
        this.data = data
            .add("inverted", SerializableDataTypes.BOOLEAN, false);
    }

    public class Instance implements Consumer<T> {

        private final SerializableData.Instance dataInstance;
        private Instance(SerializableData.Instance data) {
            this.dataInstance = data;
        }

        @Override
        public void accept(T t) {
            effect.accept(dataInstance, t);
        }

        public void write(PacketByteBuf buf) {
            buf.writeIdentifier(identifier);
            data.write(buf, dataInstance);
        }

        public JsonObject toJson() {

            JsonObject jsonObject = data.write(dataInstance);
            jsonObject.addProperty("type", identifier.toString());

            return jsonObject;

        }

    }

    @Override
    public Identifier getSerializerId() {
        return identifier;
    }

    @Override
    public SerializableData getSerializableData() {
        return data;
    }

    public Instance read(JsonObject json) {
        return new Instance(data.read(json));
    }

    public Instance read(PacketByteBuf buffer) {
        return new Instance(data.read(buffer));
    }

}
