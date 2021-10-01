package io.github.apace100.apoli.integration;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;

public interface PowerReloadCallback {

    Event<PowerReloadCallback> EVENT = EventFactory.createArrayBacked(PowerReloadCallback.class,
        (listeners) -> () -> {
            for (PowerReloadCallback event : listeners) {
                event.onPowerReload();
            }
        }
    );

    void onPowerReload();
}
