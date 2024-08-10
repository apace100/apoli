package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LightLevelConditionType {

    public static boolean condition(CachedBlockPosition cachedBlock, @Nullable LightType lightType, Comparison comparison, int compareTo) {

        World world = (World) cachedBlock.getWorld();
        BlockPos pos = cachedBlock.getBlockPos();

        int lightLevel;

        if (lightType != null) {
            lightLevel = world.getLightLevel(lightType, pos);
        }

        else {

            if (world.isClient) {
                world.calculateAmbientDarkness();
            }

            lightLevel = world.getLightLevel(pos);

        }

        return comparison.compare(lightLevel, compareTo);

    }

    public static ConditionTypeFactory<CachedBlockPosition> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("light_level"),
            new SerializableData()
                .add("light_type", SerializableDataType.enumValue(LightType.class), null)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            (data, cachedBlock) -> condition(cachedBlock,
                data.get("light_type"),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
