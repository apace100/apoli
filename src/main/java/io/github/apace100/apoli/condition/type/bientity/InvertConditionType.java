package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class InvertConditionType {

    public static boolean condition(Entity actor, Entity target, Predicate<Pair<Entity, Entity>> biEntityCondition) {
        return biEntityCondition.test(new Pair<>(target, actor));
    }

    public static ConditionTypeFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("invert"),
            new SerializableData()
                .add("condition", ApoliDataTypes.BIENTITY_CONDITION),
            (data, actorAndTarget) -> condition(actorAndTarget.getLeft(), actorAndTarget.getRight(),
                data.get("condition")
            )
        );
    }

}
