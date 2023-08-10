package io.github.apace100.apoli.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;

public class WorldUtil {

    public static boolean inSnow(World world, BlockPos... blockPositions) {
        return Arrays.stream(blockPositions)
            .anyMatch(blockPos -> {
                Biome biome = world.getBiome(blockPos).value();
                return biome.getPrecipitation(blockPos) == Biome.Precipitation.SNOW
                    && isRainingAndExposed(world, blockPos);
            });
    }

    public static boolean inThunderstorm(World world, BlockPos... blockPositions) {
        return Arrays.stream(blockPositions)
            .anyMatch(blockPos -> world.isThundering() && isRainingAndExposed(world, blockPos));
    }

    private static boolean isRainingAndExposed(World world, BlockPos blockPos) {
        return world.isRaining()
            && world.isSkyVisible(blockPos)
            && world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, blockPos).getY() < blockPos.getY();
    }

}
