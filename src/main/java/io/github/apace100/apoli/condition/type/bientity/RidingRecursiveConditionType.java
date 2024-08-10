package io.github.apace100.apoli.condition.type.bientity;

import net.minecraft.entity.Entity;

import java.util.Objects;

public class RidingRecursiveConditionType {

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
