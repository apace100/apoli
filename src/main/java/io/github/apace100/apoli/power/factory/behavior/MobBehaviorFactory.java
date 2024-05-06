package io.github.apace100.apoli.power.factory.behavior;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.factory.Factory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;

public class MobBehaviorFactory<T extends MobBehavior> implements Factory {
    private final Identifier id;
    protected SerializableData data;
    private final BiFunction<SerializableData.Instance, MobEntity, T> factoryConstructor;

    public MobBehaviorFactory(Identifier id, SerializableData data, BiFunction<SerializableData.Instance, MobEntity, T> factoryConstructor) {
        this.id = id;
        this.data = data;
        this.factoryConstructor = factoryConstructor;
    }

    public Identifier getSerializerId() {
        return id;
    }

    @Override
    public SerializableData getSerializableData() {
        return data;
    }

    public SerializableData getData() {
        return this.data;
    }

    public class Instance implements Function<MobEntity, T> {
        private final SerializableData.Instance dataInstance;

        private Instance(SerializableData.Instance data) {
            this.dataInstance = data;
        }

        public void write(PacketByteBuf buffer) {
            buffer.writeIdentifier(id);
            data.write(buffer, dataInstance);
        }

        public JsonObject toJson() {
            JsonObject jsonObject = data.write(dataInstance);
            jsonObject.addProperty("type", id.toString());

            return jsonObject;
        }

        @Override
        public T apply(MobEntity mob) {
            return factoryConstructor.apply(dataInstance, mob);
        }
    }

    public Instance read(JsonObject json) {
        return new Instance(data.read(json));
    }

    public Instance read(PacketByteBuf buffer) {
        return new Instance(data.read(buffer));
    }
}