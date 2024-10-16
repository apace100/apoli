package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.mixin.EntityAccessor;
import net.minecraft.entity.Entity;

public class InRainEntityConditionType extends EntityConditionType {

    public static boolean condition(Entity entity) {
        return ((EntityAccessor) entity).callIsBeingRainedOn();
    }

    @Override
    public boolean test(Entity entity) {
        return ((EntityAccessor) entity).callIsBeingRainedOn();
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.IN_RAIN;
    }

}
