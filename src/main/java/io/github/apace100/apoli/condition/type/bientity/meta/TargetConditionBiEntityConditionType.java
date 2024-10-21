package io.github.apace100.apoli.condition.type.bientity.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

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
    public ConditionConfiguration<?> configuration() {
        return BiEntityConditionTypes.TARGET_CONDITION;
    }

    @Override
    public boolean test(Entity actor, Entity target) {
        return targetCondition.test(target);
    }

    public static boolean condition(Entity target, Predicate<Entity> condition) {
        return condition.test(target);
    }

    public static ConditionTypeFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("target_condition"),
            new SerializableData()
                .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, actorAndTarget) -> condition(actorAndTarget.getRight(),
                data.get("condition")
            )
        );
    }

}
