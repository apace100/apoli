package io.github.apace100.apoli.condition.type.bientity;

import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;

import java.util.Objects;

public class AttackerConditionType {

    public static boolean condition(Entity actor, Entity target) {
        return target instanceof Attackable attackable
            && Objects.equals(actor, attackable.getLastAttacker());
    }

}
