package io.github.apace100.apoli.power.factory.condition.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class UndirectedCondition {

    public static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {
        Predicate<Pair<Entity, Entity>> biEntityCondition = data.get("condition");
        return biEntityCondition.test(actorAndTarget)
            || biEntityCondition.test(new Pair<>(actorAndTarget.getRight(), actorAndTarget.getLeft()));
    }

    public static ConditionFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("undirected"),
            new SerializableData()
                .add("condition", ApoliDataTypes.BIENTITY_CONDITION),
            UndirectedCondition::condition
        );
    }

}
