package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import net.minecraft.entity.Entity;

import java.util.Objects;

public class RidingRecursiveBiEntityConditionType extends BiEntityConditionType {

    @Override
    public ConditionConfiguration<?> configuration() {
        return BiEntityConditionTypes.RIDING_RECURSIVE;
    }

    @Override
    public boolean test(Entity actor, Entity target) {
        return condition(actor, target);
    }

    public static boolean condition(Entity actor, Entity target) {

        if (actor == null || target == null || !actor.hasVehicle()) {
            return false;
        }

        Entity vehicle = actor.getVehicle();
        while (vehicle != null) {

            if (Objects.equals(vehicle, target)) {
                return true;
            }

            vehicle = vehicle.getVehicle();

        }

        return false;

    }

}
