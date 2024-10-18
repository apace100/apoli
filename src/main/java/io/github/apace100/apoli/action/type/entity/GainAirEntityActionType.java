package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class GainAirEntityActionType extends EntityActionType {

    public static final DataObjectFactory<GainAirEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("value", SerializableDataTypes.INT),
        data -> new GainAirEntityActionType(
            data.get("value")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("value", actionType.value)
    );

    private final int value;

    public GainAirEntityActionType(int value) {
        this.value = value;
    }

    @Override
    protected void execute(Entity entity) {

        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.setAir(Math.min(livingEntity.getAir() + value, livingEntity.getMaxAir()));
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.GAIN_AIR;
    }

}
