package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

public class DistanceConditionType {

    public static boolean condition(Entity actor, Entity target, Comparison comparison, double compareTo) {
        return actor != null
            && target != null
            && comparison.compare(actor.getPos().squaredDistanceTo(target.getPos()), compareTo * compareTo);
    }

    public static ConditionTypeFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("distance"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.DOUBLE),
            (data, actorAndTarget) -> condition(actorAndTarget.getLeft(), actorAndTarget.getRight(),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
