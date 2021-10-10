package io.github.apace100.apoli.integration;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Similar to PowerReloadCallback, except it's also used on the client and thus is no indication that powers are read
 * from JSON. Useful for clearing any caches you have based on power data, for example.
 */
public interface PowerClearCallback {

    Event<PowerClearCallback> EVENT = EventFactory.createArrayBacked(PowerClearCallback.class,
        (listeners) -> () -> {
            for (PowerClearCallback event : listeners) {
                event.onPowerClear();
            }
        }
    );

    void onPowerClear();
}
