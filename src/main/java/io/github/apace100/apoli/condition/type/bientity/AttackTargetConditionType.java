package io.github.apace100.apoli.condition.type.bientity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;

public class AttackTargetConditionType {

    public static boolean condition(Entity actor, Entity target) {

        if (actor == null || target == null) {
            return false;
        }

        return (actor instanceof MobEntity mobActor && target.equals(mobActor.getTarget()))
            || (actor instanceof Angerable angerableActor && target.equals(angerableActor.getTarget()));

    }

}
