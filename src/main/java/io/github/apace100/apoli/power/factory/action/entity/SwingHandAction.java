package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

public class SwingHandAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if(entity instanceof LivingEntity living) {
            living.swingHand((Hand) data.get("hand"));
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("swing_hand"),
            new SerializableData()
                .add("hand", SerializableDataTypes.HAND, Hand.MAIN_HAND),
            SwingHandAction::action
        );
    }
}
