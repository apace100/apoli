package io.github.apace100.apoli.behavior;

import com.google.gson.JsonObject;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;

public class BehaviorFactory<T extends MobBehavior> {
    private final Identifier identifier;
    protected SerializableData data;
    private final BiFunction<SerializableData.Instance, MobEntity, T> factoryConstructor;

    public BehaviorFactory(Identifier id, SerializableData data, BiFunction<SerializableData.Instance, MobEntity, T> factoryConstructor) {
        this.identifier = id;
        this.data = data;
        this.factoryConstructor = factoryConstructor;
    }

    public Identifier getSerializerId() {
        return identifier;
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
            buffer.writeIdentifier(identifier);
            data.write(buffer, dataInstance);
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