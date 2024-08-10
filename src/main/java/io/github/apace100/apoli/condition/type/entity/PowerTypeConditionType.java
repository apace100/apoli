package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class PowerTypeConditionType {

    public static boolean condition(Entity entity, PowerTypeFactory<?> powerTypeFactory) {
        return PowerHolderComponent.KEY.maybeGet(entity)
            .stream()
            .flatMap(pc -> pc.getPowerTypes().stream())
            .map(PowerType::getPower)
            .map(Power::getFactoryInstance)
            .map(PowerTypeFactory.Instance::getFactory)
            .anyMatch(powerTypeFactory::equals);
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("power_type"),
            new SerializableData()
                .add("power_type", ApoliDataTypes.POWER_TYPE_FACTORY),
            (data, entity) -> condition(entity,
                data.get("power_type")
            )
        );
    }

}
