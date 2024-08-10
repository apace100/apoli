package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class ActorConditionType {

    public static boolean condition(Entity actor, Predicate<Entity> condition) {
        return condition.test(actor);
    }

    public static ConditionTypeFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("actor_condition"),
            new SerializableData()
                .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, actorAndTarget) -> condition(actorAndTarget.getLeft(),
                data.get("condition")
            )
        );
    }

}
