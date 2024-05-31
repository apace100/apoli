package io.github.apace100.apoli.integration;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;

/**
 *  This callback is invoked for each power that is overridden. It will pass the ID of the power being overridden.
 */
public interface PowerOverrideCallback {

    Event<PowerOverrideCallback> EVENT = EventFactory.createArrayBacked(
        PowerOverrideCallback.class,
        callbacks -> id -> {

            for (PowerOverrideCallback callback : callbacks) {
                callback.onPowerOverride(id);
            }

        }
    );

    void onPowerOverride(Identifier id);

}
