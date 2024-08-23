package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class PassengerRecursiveConditionType {

    public static boolean condition(Entity entity, Predicate<Pair<Entity, Entity>> biEntityCondition, Comparison comparison, int compareTo) {

        int matches = (int) entity.getPassengerList()
            .stream()
            .flatMap(Entity::streamSelfAndPassengers)
            .map(passenger -> new Pair<>(entity, passenger))
            .filter(biEntityCondition)
            .count();

        return comparison.compare(matches, compareTo);

    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("passenger_recursive"),
            new SerializableData()
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
                .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> condition(entity,
                data.getOrElse("bientity_condition", actorAndTarget -> true),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
