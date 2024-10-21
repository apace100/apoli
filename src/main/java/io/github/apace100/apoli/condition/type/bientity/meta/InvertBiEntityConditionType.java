package io.github.apace100.apoli.condition.type.bientity.meta;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class InvertBiEntityConditionType extends BiEntityConditionType {

    public static final TypedDataObjectFactory<InvertBiEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("condition", BiEntityCondition.DATA_TYPE),
        data -> new InvertBiEntityConditionType(
            data.get("condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("condition", conditionType.biEntityCondition)
    );

    private final BiEntityCondition biEntityCondition;

    public InvertBiEntityConditionType(BiEntityCondition biEntityCondition) {
        this.biEntityCondition = biEntityCondition;
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BiEntityConditionTypes.INVERT;
    }

    @Override
    public boolean test(Entity actor, Entity target) {
        return biEntityCondition.test(target, actor);
    }

}
