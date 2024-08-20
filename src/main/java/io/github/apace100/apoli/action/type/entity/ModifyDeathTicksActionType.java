package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class ModifyDeathTicksActionType {

    public static void action(Entity entity, Modifier modifier) {

        if (entity instanceof LivingEntity living) {
            living.deathTime = (int) modifier.apply(entity, living.deathTime);
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(Apoli.identifier("modify_death_ticks"),
            new SerializableData()
                .add("modifier", Modifier.DATA_TYPE),
            (data, entity) -> action(entity,
                data.get("modifier")
            )
        );
    }

}
