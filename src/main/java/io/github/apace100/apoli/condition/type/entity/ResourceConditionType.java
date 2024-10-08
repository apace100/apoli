package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.PowerUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class ResourceConditionType {

    public static boolean condition(Entity entity, PowerReference power, Comparison comparison, int compareTo) {
        return comparison.compare(PowerUtil.getResourceValue(power.getType(entity)), compareTo);
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("resource"),
            new SerializableData()
                .add("resource", ApoliDataTypes.RESOURCE_REFERENCE)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> condition(entity,
                data.get("resource"),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
