package io.github.apace100.apoli.power.factory.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.apoli.util.IdentifierAlias;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.Optional;

public class ConditionType<T> {

    private final String conditionTypeName;
    private final Registry<ConditionFactory<T>> conditionRegistry;

    public ConditionType(String conditionTypeName, Registry<ConditionFactory<T>> conditionRegistry) {
        this.conditionTypeName = conditionTypeName;
        this.conditionRegistry = conditionRegistry;
    }

    public void write(PacketByteBuf buf, ConditionFactory<T>.Instance conditionInstance) {
        conditionInstance.write(buf);
    }

    public ConditionFactory<T>.Instance read(PacketByteBuf buf) {

        Identifier conditionFactoryId = buf.readIdentifier();
        Optional<ConditionFactory<T>> conditionFactory = conditionRegistry.getOrEmpty(conditionFactoryId);

        return conditionFactory
            .orElseThrow(() -> new JsonSyntaxException(conditionTypeName + " \"" + conditionFactoryId + "\" was not registered."))
            .read(buf);

    }

    public ConditionFactory<T>.Instance read(JsonElement jsonElement) {

        if (!jsonElement.isJsonObject()) {
            throw new JsonSyntaxException(conditionTypeName + " has to be a JSON object!");
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (!jsonObject.has("type")) {
            throw new JsonSyntaxException(conditionTypeName + " JSON requires a \"type\" identifier!");
        }

        Identifier conditionFactoryId = new Identifier(JsonHelper.getString(jsonObject, "type"));
        Optional<ConditionFactory<T>> conditionFactory = conditionRegistry.getOrEmpty(conditionFactoryId);

        if (conditionFactory.isEmpty() && IdentifierAlias.hasAlias(conditionFactoryId)) {
            conditionFactory = conditionRegistry.getOrEmpty(IdentifierAlias.resolveAlias(conditionFactoryId));
        }

        return conditionFactory
            .orElseThrow(() -> new JsonSyntaxException(conditionTypeName + " type \"" + conditionFactoryId + "\" is not registered."))
            .read(jsonObject);

    }
}
