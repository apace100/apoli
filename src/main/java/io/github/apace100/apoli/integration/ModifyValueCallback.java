package io.github.apace100.apoli.integration;

import io.github.apace100.apoli.power.ValueModifyingPower;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.util.List;

public interface ModifyValueCallback {

    Event<ModifyValueCallback> EVENT = EventFactory.createArrayBacked(ModifyValueCallback.class,
        (listeners) -> (entity, powerClass, baseValue, modifiers) -> {
            for (ModifyValueCallback event : listeners) {
                event.collectModifiers(entity, powerClass, baseValue, modifiers);
            }
        }
    );

    void collectModifiers(LivingEntity entity, Class<? extends ValueModifyingPower> powerClass, double baseValue, List<EntityAttributeModifier> modifiers);
}
