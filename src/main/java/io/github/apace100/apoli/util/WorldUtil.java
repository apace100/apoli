package io.github.apace100.apoli.util;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.BlockCollisionSpliteratorAccess;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class WorldUtil {

    public static Iterable<VoxelShape> getOriginalBlockCollisions(World world, @Nullable Entity entity, Box box) {

        BlockCollisionSpliterator<VoxelShape> spliterator = new BlockCollisionSpliterator<>(world, entity, box, false, (pos, voxelShape) -> voxelShape);
        ((BlockCollisionSpliteratorAccess) spliterator).apoli$setGetOriginalShapes(true);

        return () -> spliterator;

    }

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
