package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

public class SwingHandEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<SwingHandEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("hand", SerializableDataTypes.HAND, Hand.MAIN_HAND),
        data -> new SwingHandEntityActionType(
            data.get("hand")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("hand", actionType.hand)
    );

    private final Hand hand;

    public SwingHandEntityActionType(Hand hand) {
        this.hand = hand;
    }

    @Override
    protected void execute(Entity entity) {

        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.swingHand(hand, true);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.SWING_HAND;
    }

}
