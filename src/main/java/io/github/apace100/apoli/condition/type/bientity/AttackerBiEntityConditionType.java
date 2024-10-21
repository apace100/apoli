package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;

import java.util.Objects;

public class AttackerBiEntityConditionType extends BiEntityConditionType {

    @Override
    public ConditionConfiguration<?> configuration() {
        return BiEntityConditionTypes.ATTACKER;
    }

    @Override
    public boolean test(Entity actor, Entity target) {
        return condition(actor, target);
    }

    public static boolean condition(Entity actor, Entity target) {
        return target instanceof Attackable attackable
            && Objects.equals(actor, attackable.getLastAttacker());
    }

}
