package io.github.apace100.apoli.condition.type.bientity.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class BothBiEntityConditionType extends BiEntityConditionType {

    public static final TypedDataObjectFactory<BothBiEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("condition", EntityCondition.DATA_TYPE),
        data -> new BothBiEntityConditionType(
            data.get("condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("condition", conditionType.entityCondition)
    );

    private final EntityCondition entityCondition;

    public BothBiEntityConditionType(EntityCondition entityCondition) {
        this.entityCondition = entityCondition;
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BiEntityConditionTypes.BOTH;
    }

    @Override
    public boolean test(Entity actor, Entity target) {
        return entityCondition.test(actor)
            && entityCondition.test(target);
    }

}
