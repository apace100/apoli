package io.github.apace100.apoli.power.factory.condition;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.factory.Factory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class ConditionFactory<T> implements Factory {

    private final Identifier identifier;
    private final BiFunction<SerializableData.Instance, T, Boolean> condition;

    protected final SerializableData data;

    public ConditionFactory(Identifier identifier, SerializableData data, BiFunction<SerializableData.Instance, T, Boolean> condition) {
        this.identifier = identifier;
        this.condition = condition;
        this.data = data
            .add("inverted", SerializableDataTypes.BOOLEAN, false);
    }

    public class Instance implements Predicate<T> {

        private final SerializableData.Instance dataInstance;
        private Instance(SerializableData.Instance data) {
            this.dataInstance = data;
        }

        @Override
        public final boolean test(T t) {
            return dataInstance.getBoolean("inverted") != isFulfilled(t);
        }

        public boolean isFulfilled(T t) {
            return condition.apply(dataInstance, t);
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
