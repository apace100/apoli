package io.github.apace100.apoli.power.factory.action;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.apoli.integration.PostActionLoadCallback;
import io.github.apace100.apoli.integration.PreActionLoadCallback;
import io.github.apace100.apoli.util.NamespaceAlias;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.registry.Registry;

import java.util.Optional;

public class ActionType<T> {

    private final String actionTypeName;
    private final Registry<ActionFactory<T>> actionFactoryRegistry;

    public ActionType(String actionTypeName, Registry<ActionFactory<T>> actionFactoryRegistry) {
        this.actionTypeName = actionTypeName;
        this.actionFactoryRegistry = actionFactoryRegistry;
    }

    public void write(PacketByteBuf buf, ActionFactory.Instance actionInstance) {
        actionInstance.write(buf);
    }

    public ActionFactory<T>.Instance read(PacketByteBuf buf) {
        Identifier type = buf.readIdentifier();
        ActionFactory<T> actionFactory = actionFactoryRegistry.get(type);
        if(actionFactory == null) {
            throw new JsonSyntaxException(actionTypeName + " \"" + type + "\" was not registered.");
        }
        return actionFactory.read(buf);
    }

    public ActionFactory<T>.Instance read(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            PreActionLoadCallback.EVENT.invoker().onPreActionLoad(actionFactoryRegistry, obj);
            if(!obj.has("type")) {
                throw new JsonSyntaxException(actionTypeName + " json requires \"type\" identifier.");
            }
            String typeIdentifier = JsonHelper.getString(obj, "type");
            Identifier type = Identifier.tryParse(typeIdentifier);
            Optional<ActionFactory<T>> optionalAction = actionFactoryRegistry.getOrEmpty(type);
            if(!optionalAction.isPresent()) {
                if(NamespaceAlias.hasAlias(type)) {
                    optionalAction = actionFactoryRegistry.getOrEmpty(NamespaceAlias.resolveAlias(type));
                }
                if(!optionalAction.isPresent()) {
                    throw new JsonSyntaxException(actionTypeName + " json type \"" + type.toString() + "\" is not defined.");
                }
            }
            ActionFactory<T>.Instance actioh = optionalAction.get().read(obj);
            PostActionLoadCallback.EVENT.invoker().onPostActionLoad(type, actionFactoryRegistry, optionalAction.get().data.read(obj), actioh, obj);
            return actioh;
        }
        throw new JsonSyntaxException(actionTypeName + " has to be a JsonObject!");
    }
}
