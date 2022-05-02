package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

public class ModifyDeathTicksAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if(entity instanceof LivingEntity living) {
            living.deathTime = (int)data.<Modifier>get("modifier").apply(entity, living.deathTime);
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("modify_death_ticks"),
            new SerializableData()
                .add("modifier", Modifier.DATA_TYPE),
            ModifyDeathTicksAction::action
        );
    }
}
