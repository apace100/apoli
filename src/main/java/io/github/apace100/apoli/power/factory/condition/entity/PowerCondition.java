package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import java.util.concurrent.atomic.AtomicBoolean;

public class PowerCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        PowerType<?> powerType = data.get("power");
        Identifier powerSource = data.get("source");

        AtomicBoolean bl = new AtomicBoolean(false);
        PowerHolderComponent.KEY.maybeGet(entity).ifPresent(
            phc -> bl.set(powerSource != null ? phc.hasPower(powerType, powerSource) : phc.hasPower(powerType))
        );

        return bl.get();

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("power"),
            new SerializableData()
                .add("power", ApoliDataTypes.POWER_TYPE)
                .add("source", SerializableDataTypes.IDENTIFIER, null),
            PowerCondition::condition
        );
    }

}
