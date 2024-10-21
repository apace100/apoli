package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

import java.util.Collection;

public class PowerTypeEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<PowerTypeEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("power_type", ApoliDataTypes.POWER_TYPE_FACTORY),
        data -> new PowerTypeEntityConditionType(
            data.get("power_type")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("power_type", conditionType.powerType)
    );

    private final PowerTypeFactory<?> powerType;

    public PowerTypeEntityConditionType(PowerTypeFactory<?> powerType) {
        this.powerType = powerType;
    }

    @Override
    public boolean test(Entity entity) {
        return PowerHolderComponent.getOptional(entity)
            .stream()
            .map(PowerHolderComponent::getPowerTypes)
            .flatMap(Collection::stream)
            .map(PowerType::getPower)
            .map(Power::getFactoryInstance)
            .map(PowerTypeFactory.Instance::getFactory)
            .anyMatch(powerType::equals);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.POWER_TYPE;
    }

}
