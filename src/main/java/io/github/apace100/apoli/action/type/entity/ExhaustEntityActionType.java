package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class ExhaustEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ExhaustEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("amount", SerializableDataTypes.FLOAT),
        data -> new ExhaustEntityActionType(
            data.get("amount")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("amount", actionType.amount)
    );

    private final float amount;

    public ExhaustEntityActionType(float amount) {
        this.amount = amount;
    }

    @Override
    protected void execute(Entity entity) {

        if (entity instanceof PlayerEntity player) {
            player.addExhaustion(amount);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.EXHAUST;
    }

}
