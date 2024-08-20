package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class HealActionType {

    public static void action(Entity entity, float amount) {

        if (entity instanceof LivingEntity living) {
            living.heal(amount);
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("heal"),
            new SerializableData()
                .add("amount", SerializableDataTypes.FLOAT),
            (data, entity) -> action(entity,
                data.get("amount")
            )
        );
    }

}
