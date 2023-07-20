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
import org.jetbrains.annotations.Nullable;

public class PowerCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        PowerType<?> powerType = data.get("power");
        Identifier powerSource = data.get("source");

        return PowerHolderComponent.KEY.maybeGet(entity)
            .map(component -> hasPower(component, powerType, powerSource))
            .orElse(false);

    }

    private static boolean hasPower(PowerHolderComponent component, PowerType<?> powerType, @Nullable Identifier powerSource) {
        return powerSource != null ? component.hasPower(powerType, powerSource) : component.hasPower(powerType);
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
