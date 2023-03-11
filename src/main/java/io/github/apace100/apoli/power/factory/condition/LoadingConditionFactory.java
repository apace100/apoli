package io.github.apace100.apoli.power.factory.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.apoli.power.factory.Factory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.NamespaceAlias;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.Optional;
import java.util.function.Function;

public class LoadingConditionFactory implements Factory {

    private final Identifier identifier;
    protected SerializableData data;
    private final Function<SerializableData.Instance, Boolean> condition;

    public LoadingConditionFactory(Identifier identifier, SerializableData data, Function<SerializableData.Instance, Boolean> condition) {
        this.identifier = identifier;
        this.condition = condition;
        this.data = data;
        this.data.add("inverted", SerializableDataTypes.BOOLEAN, false);
    }

    public class Instance {

        private final SerializableData.Instance dataInstance;

        private Instance(SerializableData.Instance data) {
            this.dataInstance = data;
        }

        public final boolean test() {
            boolean fulfilled = isFulfilled();
            if(dataInstance.getBoolean("inverted")) {
                return !fulfilled;
            } else {
                return fulfilled;
            }
        }

        public boolean isFulfilled() {
            return condition.apply(dataInstance);
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

    public static LoadingConditionFactory.Instance read(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            if(!obj.has("type")) {
                throw new JsonSyntaxException("LoadingCondition json requires \"type\" identifier.");
            }
            String typeIdentifier = JsonHelper.getString(obj, "type");
            Identifier type = Identifier.tryParse(typeIdentifier);
            Optional<LoadingConditionFactory> optionalCondition = ApoliRegistries.LOADING_CONDITION.getOrEmpty(type);
            if(!optionalCondition.isPresent()) {
                if(NamespaceAlias.hasAlias(type)) {
                    optionalCondition = ApoliRegistries.LOADING_CONDITION.getOrEmpty(NamespaceAlias.resolveAlias(type));
                }
                if(!optionalCondition.isPresent()) {
                    throw new JsonSyntaxException("LoadingCondition json type \"" + type + "\" is not defined.");
                }
            }
            return optionalCondition.get().read(obj);
        }
        throw new JsonSyntaxException("LoadingCondition has to be a JsonObject!");
    }
}
