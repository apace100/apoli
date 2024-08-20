package io.github.apace100.apoli.integration;

import io.github.apace100.apoli.power.type.ValueModifyingPowerType;
import io.github.apace100.apoli.util.modifier.Modifier;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;

import java.util.List;

public interface ModifyValueCallback {

    Event<ModifyValueCallback> EVENT = EventFactory.createArrayBacked(ModifyValueCallback.class,
        (listeners) -> (entity, powerClass, baseValue, modifiers) -> {
            for (ModifyValueCallback event : listeners) {
                event.collectModifiers(entity, powerClass, baseValue, modifiers);
            }
        }
    );

    void collectModifiers(Entity entity, Class<? extends ValueModifyingPowerType> powerClass, double baseValue, List<Modifier> modifiers);

}
