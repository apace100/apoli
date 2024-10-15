package io.github.apace100.apoli.condition.context;

import io.github.apace100.apoli.util.context.TypeConditionContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record EntityConditionContext(Entity entity) implements TypeConditionContext {

	public World world() {
		return entity().getWorld();
	}

	public BlockPos blockPos() {
		return entity().getBlockPos();
	}

}
