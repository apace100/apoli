package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class HealEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<HealEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("amount", SerializableDataTypes.FLOAT),
        data -> new HealEntityActionType(
            data.get("amount")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("amount", actionType.amount)
    );

    private final float amount;

    public HealEntityActionType(float amount) {
        this.amount = amount;
    }

    @Override
    protected void execute(Entity entity) {

        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.heal(amount);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.HEAL;
    }

}
