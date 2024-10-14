package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;

import java.util.Optional;

public class RidingEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<RidingEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty()),
        data -> new RidingEntityConditionType(
            data.get("bientity_condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("bientity_condition", conditionType.biEntityCondition)
    );

    private final Optional<BiEntityCondition> biEntityCondition;

    public RidingEntityConditionType(Optional<BiEntityCondition> biEntityCondition) {
        this.biEntityCondition = AbstractCondition.setPowerType(biEntityCondition, getPowerType());
    }

    @Override
    public boolean test(Entity entity) {
        Entity vehicle = entity.getVehicle();
        return vehicle != null
            && biEntityCondition.map(condition -> condition.test(entity, vehicle)).orElse(true);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.RIDING;
    }

}
