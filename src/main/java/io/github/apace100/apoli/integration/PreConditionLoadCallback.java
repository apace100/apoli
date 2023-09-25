package io.github.apace100.apoli.integration;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.registry.Registry;

/**
 * This callback is fired for each condition that is loaded. It contains the factory of the condition that is being read, as well as
 * the JSON data, and can thus be used to read additional data from conditions.
 */
public interface PreConditionLoadCallback {

    Event<PreConditionLoadCallback> EVENT = EventFactory.createArrayBacked(PreConditionLoadCallback.class,
        (listeners) -> (factoryRegistry, json) -> {
            for (PreConditionLoadCallback event : listeners) {
                event.onPreConditionLoad(factoryRegistry, json);
            }
        }
    );

    void onPreConditionLoad(Registry<?> factoryRegistry, JsonObject json);
}
