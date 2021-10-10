package io.github.apace100.apoli.integration;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@Deprecated
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
