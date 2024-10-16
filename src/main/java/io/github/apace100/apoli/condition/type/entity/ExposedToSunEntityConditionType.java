package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.util.Comparison;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ExposedToSunEntityConditionType extends EntityConditionType {

    private static final InRainEntityConditionType IN_RAIN = new InRainEntityConditionType();
    private static final BrightnessEntityConditionType BRIGHTNESS = new BrightnessEntityConditionType(Comparison.GREATER_THAN, 0.5F);
    private static final ExposedToSkyEntityConditionType EXPOSED_TO_SKY = new ExposedToSkyEntityConditionType();

    @Override
    public boolean test(Entity entity) {
        World world = entity.getWorld();
        return world.isDay()
            && !IN_RAIN.test(entity)
            && BRIGHTNESS.test(entity)
            && EXPOSED_TO_SKY.test(entity);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.EXPOSED_TO_SUN;
    }

}
