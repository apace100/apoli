package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.mixin.EntityAccessor;
import net.minecraft.entity.Entity;

public class InRainConditionType {

    public static boolean condition(Entity entity) {
        return ((EntityAccessor) entity).callIsBeingRainedOn();
    }

}
