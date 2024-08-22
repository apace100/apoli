package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.power.factory.condition.EntityConditions;
import io.github.apace100.apoli.power.type.EntitySetPowerType;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

public class EntitySetSizeCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(entity).orElse(null);
        Power power = data.get("set");

        if (component == null || power == null || !(component.getPowerType(power) instanceof EntitySetPowerType entitySetPower)) {
            return false;
        }

        Comparison comparison = data.get("comparison");
        int compareTo = data.get("compare_to");

        return comparison.compare(entitySetPower.size(), compareTo);

    }

    public static ConditionFactory<Entity> getFactory() {

        ConditionFactory<Entity> factory = new ConditionFactory<>(
            Apoli.identifier("entity_set_size"),
            new SerializableData()
                .add("set", ApoliDataTypes.POWER_REFERENCE)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            EntitySetSizeCondition::condition
        );

        EntityConditions.ALIASES.addPathAlias("set_size", factory.getSerializerId().getPath());
        return factory;

    }
}
