package io.github.apace100.apoli.power.factory.condition.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

public class RidingRecursiveCondition {

    public static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {

        Entity actor = actorAndTarget.getLeft();
        Entity target = actorAndTarget.getRight();

        if ((actor == null || target == null) || !actor.hasVehicle()) {
            return false;
        }

        Entity vehicle = actor.getVehicle();
        while (vehicle != null && !vehicle.equals(target)) {
            vehicle = vehicle.getVehicle();
        }

        return target.equals(vehicle);

    }

    public static ConditionFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("riding_recursive"),
            new SerializableData(),
            RidingRecursiveCondition::condition
        );
    }

}
