package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.mixin.EntityAccessor;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExposedToSunCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        World world = entity.getWorld();
        if (!world.isDay() || ((EntityAccessor) entity).callIsBeingRainedOn()) {
            return false;
        }

        if (world.isClient) {
            world.calculateAmbientDarkness();   //  Re-calculate the world's ambient darkness, since it's only calculated once in the client
        }

        BlockPos blockPos = BlockPos.ofFloored(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        float brightness = world.getBrightness(blockPos);

        return brightness > 0.5
            && world.isSkyVisible(blockPos);

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("exposed_to_sun"),
            new SerializableData(),
            ExposedToSunCondition::condition
        );
    }

}
