package io.github.apace100.apoli.condition.type.bientity.meta;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class ActorConditionBiEntityConditionType extends BiEntityConditionType {

    public static final TypedDataObjectFactory<ActorConditionBiEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("condition", EntityCondition.DATA_TYPE),
        data -> new ActorConditionBiEntityConditionType(
            data.get("condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("condition", conditionType.actorCondition)
    );

    private final EntityCondition actorCondition;

    public ActorConditionBiEntityConditionType(EntityCondition actorCondition) {
        this.actorCondition = actorCondition;
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BiEntityConditionTypes.ACTOR_CONDITION;
    }

    @Override
    public boolean test(Entity actor, Entity target) {
        return actorCondition.test(actor);
    }

}
