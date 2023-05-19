package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class ResourceCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        PowerType<?> powerType = data.get("resource");
        Comparison comparison = data.get("comparison");
        int compareTo = data.get("compare_to");

        return PowerHolderComponent.KEY.maybeGet(entity)
            .map(component -> comparePowerValue(component, powerType, comparison, compareTo))
            .orElse(false);

    }

    private static boolean comparePowerValue(PowerHolderComponent component, PowerType<?> powerType, Comparison comparison, int compareTo) {

        Integer powerValue = null;
        Power power = component.getPower(powerType);

        if (power instanceof VariableIntPower vip) powerValue = vip.getValue();
        else if (power instanceof CooldownPower cp) powerValue = cp.getRemainingTicks();

        return powerValue != null && comparison.compare(powerValue, compareTo);

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("resource"),
            new SerializableData()
                .add("resource", ApoliDataTypes.POWER_TYPE)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            ResourceCondition::condition
        );
    }

}
