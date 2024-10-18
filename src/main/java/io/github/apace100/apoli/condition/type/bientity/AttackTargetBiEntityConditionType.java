package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.mob.Angerable;

import java.util.Objects;

public class AttackTargetBiEntityConditionType extends BiEntityConditionType {

    @Override
    public ConditionConfiguration<?> configuration() {
        return BiEntityConditionTypes.ATTACK_TARGET;
    }

    @Override
    public boolean test(Entity actor, Entity target) {
        return condition(actor, target);
    }

    public static boolean condition(Entity actor, Entity target) {
        return (actor instanceof Targeter targeter && Objects.equals(target, targeter.getTarget()))
            || (actor instanceof Angerable angerable && Objects.equals(target, angerable.getTarget()));
    }

}
