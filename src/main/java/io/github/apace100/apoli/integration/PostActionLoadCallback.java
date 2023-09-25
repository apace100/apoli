package io.github.apace100.apoli.integration;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * This callback is fired for each action that is loaded. It contains the factory id of the action that is being read,
 * as well as the JSON data and the factory registry.
 * This also allows you to read the ActionFactory and the SerializableData instance.
 */
public interface PostActionLoadCallback {

    Event<PostActionLoadCallback> EVENT = EventFactory.createArrayBacked(PostActionLoadCallback.class,
        (listeners) -> (factoryId, factoryRegistry, dataInstance, actionInstance, jsonObject) -> {
            for (PostActionLoadCallback event : listeners) {
                event.onPostActionLoad(factoryId, factoryRegistry, dataInstance, actionInstance, jsonObject);
            }
        }
    );

    void onPostActionLoad(Identifier factoryId, Registry<?> factoryRegistry, SerializableData.Instance dataInstance, ActionFactory<?>.Instance actionInstance, JsonObject json);
}
