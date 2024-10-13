package io.github.apace100.apoli.condition.type.bientity.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class TargetConditionBiEntityConditionType extends BiEntityConditionType {

    public static final DataObjectFactory<TargetConditionBiEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
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
        this.targetCondition = AbstractCondition.setPowerType(targetCondition, getPowerType());
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
