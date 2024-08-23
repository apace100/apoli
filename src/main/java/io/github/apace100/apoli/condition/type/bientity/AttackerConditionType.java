package io.github.apace100.apoli.condition.type.bientity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class AttackerConditionType {

    public static boolean condition(Entity actor, Entity target) {

        if (actor == null || target == null) {
            return false;
        }

        return target instanceof LivingEntity livingTarget
            && actor.equals(livingTarget.getAttacker());

    }

}
