package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class PowerActiveEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<PowerActiveEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("power", ApoliDataTypes.POWER_REFERENCE),
        data -> new PowerActiveEntityConditionType(
            data.get("power")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("power", conditionType.power)
    );

    private final PowerReference power;

    public PowerActiveEntityConditionType(PowerReference power) {
        this.power = power;
    }

    @Override
    public boolean test(Entity entity) {
        return power.isActive(entity);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.POWER_ACTIVE;
    }

}
