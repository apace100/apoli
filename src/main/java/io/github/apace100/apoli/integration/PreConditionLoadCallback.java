package io.github.apace100.apoli.integration;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * This callback is fired for each condition that is loaded. It contains the factory of the condition that is being read, as well as
 * the JSON data, and can thus be used to read additional data from conditions.
 * @param <T> The parameter used in the factory registry and condition. (e.g. {@link net.minecraft.entity.Entity} for entity conditions).
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public interface PreConditionLoadCallback<T> {

    Event<PreConditionLoadCallback> EVENT = EventFactory.createArrayBacked(PreConditionLoadCallback.class,
        (listeners) -> (factoryRegistry, json) -> {
            for (PreConditionLoadCallback event : listeners) {
                event.onPreConditionLoad(factoryRegistry, json);
            }
        }
    );

    void onPreConditionLoad(Registry<ConditionFactory<T>> factoryRegistry, JsonObject json);
}
