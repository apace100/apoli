package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

import java.util.Optional;

public class PassengerRecursiveEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<PassengerRecursiveEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
            .add("compare_to", SerializableDataTypes.INT, 1),
        data -> new PassengerRecursiveEntityConditionType(
            data.get("bientity_condition"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("bientity_condition", conditionType.biEntityCondition)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final Optional<BiEntityCondition> biEntityCondition;

    private final Comparison comparison;
    private final int compareTo;

    public PassengerRecursiveEntityConditionType(Optional<BiEntityCondition> biEntityCondition, Comparison comparison, int compareTo) {
        this.biEntityCondition = biEntityCondition;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(Entity entity) {

        long matches = entity.getPassengerList()
            .stream()
            .flatMap(Entity::streamPassengersAndSelf)
            .filter(passenger -> biEntityCondition.map(condition -> condition.test(passenger, entity)).orElse(true))
            .count();

        return comparison.compare(matches, compareTo);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.PASSENGER_RECURSIVE;
    }

}
