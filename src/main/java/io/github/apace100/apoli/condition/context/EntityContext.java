package io.github.apace100.apoli.condition.context;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record EntityContext(Entity entity) {

	public World world() {
		return entity().getWorld();
	}

	public BlockPos blockPos() {
		return entity().getBlockPos();
	}

}
