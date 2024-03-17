package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BrightnessCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        World world = entity.getWorld();
        if (world.isClient) {
            world.calculateAmbientDarkness();   //  Re-calculate the world's ambient darkness, since it's only calculated once in the client
        }

        Comparison comparison = data.get("comparison");
        float compareTo = data.get("compare_to");

        BlockPos blockPos = BlockPos.ofFloored(entity.getX(), entity.getY() + entity.getEyeHeight(entity.getPose()), entity.getZ());
        float brightness = world.getBrightness(blockPos);

        return comparison.compare(brightness, compareTo);

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("brightness"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.FLOAT),
            BrightnessCondition::condition
        );
    }

}
