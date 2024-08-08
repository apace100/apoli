package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.type.CooldownPowerType;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.type.VariableIntPowerType;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class ResourceCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        Power power = data.get("resource");
        Comparison comparison = data.get("comparison");
        int compareTo = data.get("compare_to");

        return PowerHolderComponent.KEY.maybeGet(entity)
            .map(component -> comparePowerValue(component, power, comparison, compareTo))
            .orElse(false);

    }

    private static boolean comparePowerValue(PowerHolderComponent component, Power power, Comparison comparison, int compareTo) {

        Integer powerValue = null;
        PowerType powerType = component.getPowerType(power);

        if (powerType instanceof VariableIntPowerType vip) powerValue = vip.getValue();
        else if (powerType instanceof CooldownPowerType cp) powerValue = cp.getRemainingTicks();

        return powerValue != null && comparison.compare(powerValue, compareTo);

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("resource"),
            new SerializableData()
                .add("resource", ApoliDataTypes.POWER_REFERENCE)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            ResourceCondition::condition
        );
    }

}
