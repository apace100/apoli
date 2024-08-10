package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.CooldownPowerType;
import io.github.apace100.apoli.power.type.VariableIntPowerType;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class ResourceConditionType {

    public static boolean condition(Entity entity, PowerReference power, Comparison comparison, int compareTo) {
        return comparison.compare(getResourceValue(entity, power), compareTo);
    }

    private static int getResourceValue(Entity entity, PowerReference power) {
        return switch (power.getType(entity)) {
            case VariableIntPowerType varInt ->
                varInt.getValue();
            case CooldownPowerType cooldown ->
                cooldown.getRemainingTicks();
            case null, default ->
                0;
        };
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("resource"),
            new SerializableData()
                .add("resource", ApoliDataTypes.POWER_REFERENCE)
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
