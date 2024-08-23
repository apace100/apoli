package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

public class SwingHandActionType {

    public static void action(Entity entity, Hand hand) {

        if (entity instanceof LivingEntity living) {
            living.swingHand(hand, true);
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("swing_hand"),
            new SerializableData()
                .add("hand", SerializableDataTypes.HAND, Hand.MAIN_HAND),
            (data, entity) -> action(entity,
                data.get("hand")
            )
        );
    }
}
