package io.github.apace100.apoli.integration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;

/**
 * This callback is fired for each power that is loaded. It contains the ID of the power that is being read, as well as
 * the JSON data, and can thus be used to read additional data from powers. If a power is overridden by a file with
 * higher loading priority, this callback is fired again.
 */
public interface PostPowerLoadCallback {

    Event<PostPowerLoadCallback> EVENT = EventFactory.createArrayBacked(PostPowerLoadCallback.class,
        (listeners) -> (powerId, factoryId, isSubPower, json, powerType) -> {
            for (PostPowerLoadCallback event : listeners) {
                event.onPostPowerLoad(powerId, factoryId, isSubPower, json, powerType);
            }
        }
    );

    void onPostPowerLoad(Identifier powerId, Identifier factoryId, boolean isSubPower, JsonObject json, PowerType<?> powerType);
}
