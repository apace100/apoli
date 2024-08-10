package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.util.Comparison;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExposedToSunConditionType {

    public static boolean condition(Entity entity) {

        World world = entity.getWorld();
        BlockPos pos = BlockPos.ofFloored(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());

        return world.isDay()
            && !InRainConditionType.condition(entity)
            && BrightnessConditionType.condition(entity, Comparison.GREATER_THAN, 0.5F)
            && world.isSkyVisible(pos);

    }

}
