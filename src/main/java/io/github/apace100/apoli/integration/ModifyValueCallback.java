package io.github.apace100.apoli.integration;

import io.github.apace100.apoli.power.ValueModifyingPower;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.util.List;

public interface ModifyValueCallback {

    Event<ModifyValueCallback> EVENT = EventFactory.createArrayBacked(ModifyValueCallback.class,
        (listeners) -> (powerClass, baseValue, modifiers) -> {
            for (ModifyValueCallback event : listeners) {
                event.collectModifiers(powerClass, baseValue, modifiers);
            }
        }
    );

    void collectModifiers(Class<? extends ValueModifyingPower> powerClass, double baseValue, List<EntityAttributeModifier> modifiers);
}
