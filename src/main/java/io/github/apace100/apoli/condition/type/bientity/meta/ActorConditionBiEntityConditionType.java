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

public class ActorConditionBiEntityConditionType extends BiEntityConditionType {

    public static final DataObjectFactory<ActorConditionBiEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
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
        this.actorCondition = AbstractCondition.setPowerType(actorCondition, getPowerType());
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
