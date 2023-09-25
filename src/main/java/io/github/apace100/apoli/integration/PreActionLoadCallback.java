package io.github.apace100.apoli.integration;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.registry.Registry;

/**
 * This callback is fired for each action that is loaded. It contains the factory of the action that is being read, as well as
 * the JSON data, and can thus be used to read additional data from actions.
 */
public interface PreActionLoadCallback {

    Event<PreActionLoadCallback> EVENT = EventFactory.createArrayBacked(PreActionLoadCallback.class,
            (listeners) -> (factoryRegistry, json) -> {
                for (PreActionLoadCallback event : listeners) {
                    event.onPreActionLoad(factoryRegistry, json);
                }
            }
    );

    void onPreActionLoad(Registry<?> factoryRegistry, JsonObject json);
}
