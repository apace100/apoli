package io.github.apace100.apoli.power.factory.condition.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class LightLevelCondition {

    public static boolean condition(SerializableData.Instance data, CachedBlockPosition cachedBlock) {

        World world = (World) cachedBlock.getWorld();
        BlockPos blockPos = cachedBlock.getBlockPos();

        Comparison comparison = data.get("comparison");

        int compareTo = data.getInt("compare_to");
        int lightLevel;

        if (data.isPresent("light_type")) {
            lightLevel = world.getLightLevel(data.get("light_type"), blockPos);
        }

        else {

            if (world.isClient) {
                world.calculateAmbientDarkness();   //  Re-calculate the world's ambient darkness, since it's only calculated once in the client
            }

            lightLevel = world.getLightLevel(blockPos);

        }

        return comparison.compare(lightLevel, compareTo);

    }

    public static ConditionFactory<CachedBlockPosition> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("light_level"),
            new SerializableData()
                .add("light_type", SerializableDataType.enumValue(LightType.class), null)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            LightLevelCondition::condition
        );
    }

}
