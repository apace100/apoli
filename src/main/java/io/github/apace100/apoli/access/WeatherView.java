package io.github.apace100.apoli.access;

import net.minecraft.util.math.BlockPos;

public interface WeatherView {
	boolean inSnow(BlockPos... pos);
	boolean inThunderStorm(BlockPos... pos);
}
