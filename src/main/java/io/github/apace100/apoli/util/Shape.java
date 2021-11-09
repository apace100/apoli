package io.github.apace100.apoli.util;

import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public enum Shape {
    CUBE, CHEBYSHEV,
    STAR, MANHATTAN,
    SPHERE, EUCLIDEAN;

    public static Collection<BlockPos> getPositions(BlockPos center, Shape shape, int radius) {
        Set<BlockPos> positions = new HashSet<>();
        for(int i = -radius; i <= radius; i++) {
            for(int j = -radius; j <= radius; j++) {
                for(int k = -radius; k <= radius; k++) {
                    if(shape == Shape.CUBE || shape == Shape.CHEBYSHEV
                            || (shape == Shape.SPHERE || shape == Shape.EUCLIDEAN)
                                && Math.sqrt(i * i + j * j + k * k) <= radius
                            || (Math.abs(i) + Math.abs(j) + Math.abs(k)) <= radius) {
                        positions.add(new BlockPos(center.add(i, j, k)));
                    }
                }
            }
        }
        return positions;
    }

    public static double getDistance(Shape shape, double xDistance, double yDistance, double zDistance){
        return switch (shape){
            case SPHERE, EUCLIDEAN -> Math.sqrt(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance);
            case STAR, MANHATTAN -> xDistance + yDistance + zDistance;
            case CUBE, CHEBYSHEV -> Math.max(Math.max(xDistance, yDistance), zDistance);
        };
    }
}
