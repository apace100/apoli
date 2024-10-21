package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

import java.util.Optional;

public class RidingRootEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<RidingRootEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty()),
        data -> new RidingRootEntityConditionType(
            data.get("bientity_condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("bientity_condition", conditionType.biEntityCondition)
    );

    private final Optional<BiEntityCondition> biEntityCondition;

    public RidingRootEntityConditionType(Optional<BiEntityCondition> biEntityCondition) {
        this.biEntityCondition = biEntityCondition;
    }

    @Override
    public boolean test(Entity entity) {
        Entity rootVehicle = entity.getRootVehicle();
        return rootVehicle != null
            && biEntityCondition.map(condition -> condition.test(entity, rootVehicle)).orElse(true);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.RIDING_ROOT;
    }

}
