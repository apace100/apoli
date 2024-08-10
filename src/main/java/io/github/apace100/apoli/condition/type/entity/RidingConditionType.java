package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class RidingConditionType {

    public static boolean condition(Entity entity, Predicate<Pair<Entity, Entity>> biEntityCondition) {
        Entity vehicle = entity.getVehicle();
        return vehicle != null
            && biEntityCondition.test(new Pair<>(entity, vehicle));
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("riding"),
            new SerializableData()
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            (data, entity) -> condition(entity,
                data.getOrElse("bientity_condition", actorAndTarget -> true)
            )
        );
    }

}
