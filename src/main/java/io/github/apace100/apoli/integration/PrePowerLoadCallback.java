package io.github.apace100.apoli.integration;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;

/**
 * This callback is fired for each power that is loaded. It contains the ID of the power that is being read, as well as
 * the JSON data, and can thus be used to read additional data from powers. This is called **before** the loading priority
 * is checked, thus it could be that the resulting JsonObject is not even considered.
 */
public interface PrePowerLoadCallback {

    Event<PrePowerLoadCallback> EVENT = EventFactory.createArrayBacked(PrePowerLoadCallback.class,
        (listeners) -> (identifier, json) -> {
            for (PrePowerLoadCallback event : listeners) {
                event.onPrePowerLoad(identifier, json);
            }
        }
    );

    void onPrePowerLoad(Identifier identifier, JsonObject json);
}
