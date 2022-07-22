package io.github.apace100.apoli.behavior;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.NamespaceAlias;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.Optional;
import java.util.function.Function;

public class BehaviorFactory<T extends MobBehavior> {
    private final Identifier identifier;
    protected SerializableData data;
    private final Function<SerializableData.Instance, T> factory;

    public BehaviorFactory(Identifier id, SerializableData data, Function<SerializableData.Instance, T> factory) {
        this.identifier = id;
        this.data = data;
        this.factory = factory;
    }

    public Identifier getSerializerId() {
        return identifier;
    }

    public SerializableData getData() {
        return this.data;
    }

    public T read(JsonObject json) {
        SerializableData.Instance dataInstance = data.read(json);
        T mobBehaviour = factory.apply(dataInstance);
        mobBehaviour.setFactory(this);
        return mobBehaviour;
    }

    public T read(PacketByteBuf buffer) {
        SerializableData.Instance dataInstance = data.read(buffer);
        T mobBehaviour = factory.apply(dataInstance);
        mobBehaviour.setFactory(this);
        return mobBehaviour;
    }

    public static void throwExceptionMessages(JsonObject json) {
        if(json.isJsonObject()) {
            JsonObject jo = json.getAsJsonObject();
            String type = JsonHelper.getString(jo, "type");
            Identifier id = Identifier.tryParse(type);
            if (id == null) {
                throw new JsonSyntaxException("Behaviour json requires \"type\" identifier.");
            }
            Registry<BehaviorFactory> registry = ApoliRegistries.BEHAVIOR_FACTORY;
            Optional<BehaviorFactory> optionalCondition = registry.getOrEmpty(id);
            if (optionalCondition.isEmpty()) {
                if (NamespaceAlias.hasAlias(id)) {
                    optionalCondition = registry.getOrEmpty(NamespaceAlias.resolveAlias(id));
                }
                if (optionalCondition.isEmpty()) {
                    throw new JsonSyntaxException("Behaviour json type \"" + id + "\" is not defined.");
                }
            }
        }
        throw new JsonSyntaxException("Behaviour has to be a JsonObject!");
    }
}