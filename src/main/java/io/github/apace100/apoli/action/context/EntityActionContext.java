package io.github.apace100.apoli.action.context;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public record EntityActionContext(Entity entity) {

	public World world() {
		return entity().getWorld();
	}

	public Vec3d pos() {
		return entity().getPos();
	}

}
