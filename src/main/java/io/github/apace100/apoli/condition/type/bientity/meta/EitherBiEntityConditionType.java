package io.github.apace100.apoli.condition.type.bientity.meta;

import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;

public class EitherBiEntityConditionType extends BiEntityConditionType {

    public static final DataObjectFactory<EitherBiEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("condition", EntityCondition.DATA_TYPE),
        data -> new EitherBiEntityConditionType(
            data.get("condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("condition", conditionType.entityCondition)
    );

    private final EntityCondition entityCondition;

    public EitherBiEntityConditionType(EntityCondition entityCondition) {
        this.entityCondition = AbstractCondition.setPowerType(entityCondition, getPowerType());
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BiEntityConditionTypes.EITHER;
    }

    @Override
    public boolean test(Entity actor, Entity target) {
        return entityCondition.test(actor)
            || entityCondition.test(target);
    }

}