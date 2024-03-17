package io.github.apace100.apoli.power.factory.condition.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.Objects;

public class EqualCondition {

    public static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {
        return Objects.equals(actorAndTarget.getLeft(), actorAndTarget.getRight());
    }

    public static ConditionFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("equal"),
            new SerializableData(),
            EqualCondition::condition
        );
    }

}
