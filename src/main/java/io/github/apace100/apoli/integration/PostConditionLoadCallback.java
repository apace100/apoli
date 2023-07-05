package io.github.apace100.apoli.integration;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * This callback is fired for each action that is loaded. It contains the factory of the action that is being read, as well as
 * the JSON data, and can thus be used to read additional data from actions.
 * @param <T> The parameter used in the factory registry and condition. (e.g. {@link net.minecraft.entity.Entity} for entity actions).
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public interface PostConditionLoadCallback<T> {

    Event<PostConditionLoadCallback> EVENT = EventFactory.createArrayBacked(PostConditionLoadCallback.class,
        (listeners) -> (factoryId, factoryRegistry, dataInstance, conditionInstance, jsonObject) -> {
            for (PostConditionLoadCallback event : listeners) {
                event.onPostConditionLoad(factoryId, factoryRegistry, dataInstance, conditionInstance, jsonObject);
            }
        }
    );

    void onPostConditionLoad(Identifier factoryId, Registry<ConditionFactory<T>> factoryRegistry, SerializableData.Instance dataInstance, ConditionFactory<T>.Instance conditionInstance, JsonObject json);
}
