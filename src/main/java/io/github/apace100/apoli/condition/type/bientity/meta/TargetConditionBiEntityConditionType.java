package io.github.apace100.apoli.condition.type.bientity.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class TargetConditionBiEntityConditionType extends BiEntityConditionType {

    public static final TypedDataObjectFactory<TargetConditionBiEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("condition", EntityCondition.DATA_TYPE),
        data -> new TargetConditionBiEntityConditionType(
            data.get("condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("condition", conditionType.targetCondition)
    );

    private final EntityCondition targetCondition;

    public TargetConditionBiEntityConditionType(EntityCondition targetCondition) {
        this.targetCondition = targetCondition;
    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return BiEntityConditionTypes.TARGET_CONDITION;
    }

    @Override
    public boolean test(Entity actor, Entity target) {
        return targetCondition.test(target);
    }

}
