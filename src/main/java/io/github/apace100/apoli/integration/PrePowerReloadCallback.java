package io.github.apace100.apoli.integration;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface PrePowerReloadCallback {

    Event<PrePowerReloadCallback> EVENT = EventFactory.createArrayBacked(PrePowerReloadCallback.class,
        (listeners) -> () -> {
            for (PrePowerReloadCallback event : listeners) {
                event.onPrePowerReload();
            }
        }
    );

    void onPrePowerReload();
}
