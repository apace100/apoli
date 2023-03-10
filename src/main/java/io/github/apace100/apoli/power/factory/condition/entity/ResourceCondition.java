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

import java.util.concurrent.atomic.AtomicBoolean;

public class ResourceCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        PowerType<?> powerType = data.get("resource");
        Comparison comparison = data.get("comparison");
        int b = data.get("compare_to");

        AtomicBoolean bl = new AtomicBoolean(false);
        PowerHolderComponent.KEY.maybeGet(entity).ifPresent(
            phc -> {

                Integer a = null;
                Power power = phc.getPower(powerType);

                if (power instanceof VariableIntPower vip) a = vip.getValue();
                else if (power instanceof CooldownPower cp) a = cp.getRemainingTicks();

                if (a != null) bl.set(comparison.compare(a, b));

            }
        );

        return bl.get();

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
