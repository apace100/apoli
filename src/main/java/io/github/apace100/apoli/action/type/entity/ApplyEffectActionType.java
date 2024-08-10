package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Collection;
import java.util.LinkedList;

public class ApplyEffectActionType {

    public static void action(Entity entity, Collection<StatusEffectInstance> effects) {

        if (!entity.getWorld().isClient && entity instanceof LivingEntity living) {
            effects.forEach(living::addStatusEffect);
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("apply_effect"),
            new SerializableData()
                .add("effect", SerializableDataTypes.STATUS_EFFECT_INSTANCE, null)
                .add("effects", SerializableDataTypes.STATUS_EFFECT_INSTANCES, null),
            (data, entity) -> {

                Collection<StatusEffectInstance> effects = new LinkedList<>();

                data.ifPresent("effect", effects::add);
                data.ifPresent("effects", effects::addAll);

                action(entity, effects);

            }
        );
    }

}
