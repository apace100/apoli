package io.github.apace100.apoli.integration;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface PostPowerReloadCallback {

    Event<PostPowerReloadCallback> EVENT = EventFactory.createArrayBacked(PostPowerReloadCallback.class,
        (listeners) -> () -> {
            for (PostPowerReloadCallback event : listeners) {
                event.onPostPowerReload();
            }
        }
    );

    void onPostPowerReload();
}
