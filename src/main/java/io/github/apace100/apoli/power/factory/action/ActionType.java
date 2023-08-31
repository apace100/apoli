package io.github.apace100.apoli.power.factory.action;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.apoli.util.IdentifierAlias;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.Optional;

public class ActionType<T> {

    private final String actionTypeName;
    private final Registry<ActionFactory<T>> actionRegistry;

    public ActionType(String actionTypeName, Registry<ActionFactory<T>> actionRegistry) {
        this.actionTypeName = actionTypeName;
        this.actionRegistry = actionRegistry;
    }

    public void write(PacketByteBuf buf, ActionFactory<T>.Instance actionInstance) {
        actionInstance.write(buf);
    }

    public ActionFactory<T>.Instance read(PacketByteBuf buf) {

        Identifier actionFactoryId = buf.readIdentifier();
        Optional<ActionFactory<T>> actionFactory = actionRegistry.getOrEmpty(actionFactoryId);

        return actionFactory
            .orElseThrow(() -> new JsonSyntaxException(actionTypeName + " \"" + actionFactoryId + "\" was not registered."))
            .read(buf);

    }

    public ActionFactory<T>.Instance read(JsonElement jsonElement) {

        if (!jsonElement.isJsonObject()) {
            throw new JsonSyntaxException(actionTypeName + " has to be a JSON object!");
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (!jsonObject.has("type")) {
            throw new JsonSyntaxException(actionTypeName + " JSON requires a \"type\" identifier!");
        }

        Identifier actionFactoryId = new Identifier(JsonHelper.getString(jsonObject, "type"));
        Optional<ActionFactory<T>> actionFactory = actionRegistry.getOrEmpty(actionFactoryId);

        if (actionFactory.isEmpty() && IdentifierAlias.hasAlias(actionFactoryId)) {
            actionFactory = actionRegistry.getOrEmpty(IdentifierAlias.resolveAlias(actionFactoryId, IdentifierAlias.Priority.NAMESPACE));
        }

        return actionFactory
            .orElseThrow(() -> new JsonSyntaxException(actionTypeName + " type \"" + actionFactoryId + "\" is not registered."))
            .read(jsonObject);

    }
}
