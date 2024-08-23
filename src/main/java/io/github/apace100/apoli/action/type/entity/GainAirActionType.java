package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class GainAirActionType {

    public static void action(Entity entity, int value) {

        if (entity instanceof LivingEntity living) {
            living.setAir(Math.min(living.getAir() + value, living.getMaxAir()));
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("gain_air"),
            new SerializableData()
                .add("value", SerializableDataTypes.INT),
            (data, entity) -> action(entity,
                data.get("value")
            )
        );
    }

}
