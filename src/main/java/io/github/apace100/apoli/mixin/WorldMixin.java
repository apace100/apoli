package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.WeatherView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Arrays;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable, WeatherView {

	@Shadow public abstract boolean isRaining();

	@Shadow public abstract boolean isThundering();

	@Override
	public boolean inSnow(BlockPos... pos) {
		return Arrays.stream(pos).anyMatch(p -> apoli$isRainingAndExposed(p) && this.getBiome(p).value().getPrecipitation(p) == Biome.Precipitation.SNOW);
	}

	@Override
	public boolean inThunderStorm(BlockPos... pos) {
		return Arrays.stream(pos).anyMatch(p -> apoli$isRainingAndExposed(p) && this.isThundering());
	}

	@Unique
	private boolean apoli$isRainingAndExposed(BlockPos pos) {
		return this.isRaining()
			&& this.isSkyVisible(pos)
			&& this.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos).getY() < pos.getY();
	}

}
