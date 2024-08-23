package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Collection;
import java.util.LinkedList;

public class ClearEffectActionType {

    public static void action(Entity entity, Collection<RegistryEntry<StatusEffect>> effects) {

        if (entity instanceof LivingEntity living) {

            if (!effects.isEmpty()) {
                effects.forEach(living::removeStatusEffect);
            }

            else {
                living.clearStatusEffects();
            }

        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("clear_effect"),
            new SerializableData()
                .add("effect", SerializableDataTypes.STATUS_EFFECT_ENTRY, null)
                .add("effects", SerializableDataTypes.STATUS_EFFECT_ENTRIES, null),
            (data, entity) -> {

                Collection<RegistryEntry<StatusEffect>> effects = new LinkedList<>();

                data.ifPresent("effect", effects::add);
                data.ifPresent("effects", effects::addAll);

                action(entity, effects);

            }
        );
    }

}
