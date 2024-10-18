package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExposedToSkyEntityConditionType extends EntityConditionType {

    @Override
    public boolean test(Entity entity) {
        World world = entity.getWorld();
        return world.isSkyVisible(BlockPos.ofFloored(entity.getEyePos()))
            || world.isSkyVisible(entity.getBlockPos());
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.EXPOSED_TO_SKY;
    }

}
