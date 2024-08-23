package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class PowerActiveConditionType {

    public static boolean condition(Entity entity, PowerReference powerReference) {
        return powerReference.isActive(entity);
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("power_active"),
            new SerializableData()
                .add("power", ApoliDataTypes.POWER_REFERENCE),
            (data, entity) -> condition(entity,
                data.get("power")
            )
        );
    }

}
