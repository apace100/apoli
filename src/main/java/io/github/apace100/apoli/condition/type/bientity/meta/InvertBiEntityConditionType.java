package io.github.apace100.apoli.condition.type.bientity.meta;

import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;

public class InvertBiEntityConditionType extends BiEntityConditionType {

    public static final DataObjectFactory<InvertBiEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
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
        this.biEntityCondition = AbstractCondition.setPowerType(biEntityCondition, getPowerType());
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
