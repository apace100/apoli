package io.github.apace100.apoli.condition.type.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExposedToSkyConditionType {

    public static boolean condition(Entity entity) {

        World world = entity.getWorld();
        BlockPos pos = BlockPos.ofFloored(entity.getPos());

        return world.isSkyVisible(pos.up())
            || world.isSkyVisible(pos);

    }

}
