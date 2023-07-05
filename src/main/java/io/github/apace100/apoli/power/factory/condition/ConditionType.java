package io.github.apace100.apoli.power.factory.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.apoli.integration.PostActionLoadCallback;
import io.github.apace100.apoli.integration.PostConditionLoadCallback;
import io.github.apace100.apoli.integration.PreActionLoadCallback;
import io.github.apace100.apoli.integration.PreConditionLoadCallback;
import io.github.apace100.apoli.util.NamespaceAlias;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.registry.Registry;

import java.util.Optional;

public class ConditionType<T> {

    private final String conditionTypeName;
    private final Registry<ConditionFactory<T>> conditionRegistry;

    public ConditionType(String conditionTypeName, Registry<ConditionFactory<T>> conditionRegistry) {
        this.conditionTypeName = conditionTypeName;
        this.conditionRegistry = conditionRegistry;
    }

    public void write(PacketByteBuf buf, ConditionFactory.Instance conditionInstance) {
        conditionInstance.write(buf);
    }

    public ConditionFactory<T>.Instance read(PacketByteBuf buf) {
        Identifier type = Identifier.tryParse(buf.readString(32767));
        ConditionFactory<T> conditionFactory = conditionRegistry.get(type);
        return conditionFactory.read(buf);
    }

    public ConditionFactory<T>.Instance read(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            PreConditionLoadCallback.EVENT.invoker().onPreConditionLoad(conditionRegistry, obj);
            if(!obj.has("type")) {
                throw new JsonSyntaxException(conditionTypeName + " json requires \"type\" identifier.");
            }
            String typeIdentifier = JsonHelper.getString(obj, "type");
            Identifier type = Identifier.tryParse(typeIdentifier);
            Optional<ConditionFactory<T>> optionalCondition = conditionRegistry.getOrEmpty(type);
            if(!optionalCondition.isPresent()) {
                if(NamespaceAlias.hasAlias(type)) {
                    optionalCondition = conditionRegistry.getOrEmpty(NamespaceAlias.resolveAlias(type));
                }
                if(!optionalCondition.isPresent()) {
                    throw new JsonSyntaxException(conditionTypeName + " json type \"" + type.toString() + "\" is not defined.");
                }
            }
            ConditionFactory<T>.Instance condition = optionalCondition.get().read(obj);
            PostConditionLoadCallback.EVENT.invoker().onPostConditionLoad(type, conditionRegistry, optionalCondition.get().data.read(obj), condition, obj);
            return condition;
        }
        throw new JsonSyntaxException(conditionTypeName + " has to be a JsonObject!");
    }
}
