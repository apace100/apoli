package io.github.apace100.apoli.power.factory.condition.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class ActorCondition {

    public static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {
        Entity actor = actorAndTarget.getLeft();
        return actor != null && data.<Predicate<Entity>>get("condition").test(actor);
    }

    public static ConditionFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("actor_condition"),
            new SerializableData()
                .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            ActorCondition::condition
        );
    }

}
