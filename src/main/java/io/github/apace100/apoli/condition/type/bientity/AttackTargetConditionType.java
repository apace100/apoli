package io.github.apace100.apoli.condition.type.bientity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.mob.Angerable;

import java.util.Objects;

public class AttackTargetConditionType {

    public static boolean condition(Entity actor, Entity target) {
        return (actor instanceof Targeter targeter && Objects.equals(target, targeter.getTarget()))
            || (actor instanceof Angerable angerable && Objects.equals(target, angerable.getTarget()));
    }

}
